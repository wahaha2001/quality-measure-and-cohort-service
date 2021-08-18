/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark;

import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import com.amazonaws.services.s3.AmazonS3;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.ibm.cohort.cql.aws.AWSClientConfig;
import com.ibm.cohort.cql.aws.AWSClientConfigFactory;
import com.ibm.cohort.cql.aws.AWSClientFactory;
import com.ibm.cohort.cql.aws.AWSClientHelpers;
import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.CqlDebug;
import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;
import com.ibm.cohort.cql.evaluation.CqlEvaluator;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDeserializationException;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.s3.S3CqlLibraryProvider;
import com.ibm.cohort.cql.spark.data.SparkDataRow;
import com.ibm.cohort.cql.spark.data.SparkTypeConverter;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import com.ibm.cohort.cql.terminology.UnsupportedTerminologyProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import com.ibm.cohort.datarow.engine.DataRowDataProvider;
import com.ibm.cohort.datarow.engine.DataRowRetrieveProvider;
import com.ibm.cohort.datarow.model.DataRow;

import scala.Tuple2;

/**
 * Given knowledge and clinical data artifacts provided in an Amazon S3
 * compatible storage bucket, evaluate clinical queries defined in the HL7
 * clinical quality language (CQL). 
 */
public class SparkS3CqlEvaluator implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SOURCE_FACT_IDX = "__SOURCE_FACT";

    @Parameter(names = { "-h", "--help" }, description = "Print help text", help = true)
    public boolean help;

    @Parameter(names = { "-b", "--bucket" }, description = "The AWS bucket", required = false)
    public String bucket;

    @Parameter(names = { "-i", "--input-path" }, description = "Key prefix for objects in the AWS bucket that contain the clinical data used in calculations.", required = true)
    public String inputPath;

    @Parameter(names = { "-o", "--output-path" }, description = "Key name of the object that will be written to the AWS bucket as a result of CQL evaluation. A separate output file will be written for each context that is evaluated.")
    public String outputPath;

    @Parameter(names = { "-m", "--model-info-path" }, description = "Key prefix for objects in AWS bucket that are CQL model definitions.", required = true)
    public String modelInfoPath;

    @Parameter(names = { "-c", "--cql-path" }, description = "Key prefix for objects in the AWS bucket that contain CQL library definitions.", required = true)
    public String cqlPath;

    @Parameter(names = { "-l", "--library" }, description = "Name of the CQL library to evaluate.", required = true)
    public String libraryId;

    @Parameter(names = { "-v", "--library-version" }, description = "Version of the CQL library to evaluate.", required = true)
    public String libraryVersion;

    @Parameter(names = { "--library-format" }, description = "Format of the CQL library to evaluate (CQL|ELM).", required = true)
    public CqlLibraryDescriptor.Format libraryFormat;

    @Parameter(names = { "-e", "--expression" }, description = "One or more expressions names in the CQL library that should be evaluated. When not provided, all expresions will be evaluated.", required = false)
    public Set<String> expressions;

    @Parameter(names = { "-f", "--facts" }, description = "List of datafiles and optional datatypes required to support CQL evaluation. Pairs are delimited by a colon and are of the form fileId:dataType. If dataType is not provided, the fileId is assumed to be the dataType.", required = true)
    public List<String> facts;

    @Parameter(names = { "-n", "--context-column" }, description = "The context column", required = true)
    public String contextColumn;

    @Parameter(names = { "-a", "--aggregate-on-context" }, description = "The context column")
    public boolean aggregateOnContext = false;
    
    @Parameter(names = { "--debug" }, description = "Enabled CQL debug logging")
    public boolean debug = false;

    public SparkTypeConverter typeConverter;
    
    public void run(PrintStream out) {
        AWSClientConfig awsConfig = AWSClientConfigFactory.fromEnvironment();

        boolean useJava8API = isUseJava8APIDatetime();
        
        SparkSession.Builder sparkBuilder = SparkSession.builder();
        sparkBuilder.config("spark.sql.datetime.java8API.enabled", String.valueOf(useJava8API));
        withS3Config(sparkBuilder, awsConfig);

        try (SparkSession spark = sparkBuilder.getOrCreate()) {
            this.typeConverter = new SparkTypeConverter(useJava8API);
            
            JavaPairRDD<Object, Row> allData = readAllInputData(spark);

            // TODO - for each aggregation context (claim, admission, episode, person)
            JavaPairRDD<Object, List<Row>> rowsByContextId = aggregateByContext(allData);

            // TODO - Evaluate more than one CQL library
            JavaPairRDD<Object, Map<String, Object>> resultsByContext = rowsByContextId
                    .mapToPair(this::evaluate);
            
            String batchID = UUID.randomUUID().toString();
            String outputURI = AWSClientHelpers.toS3Url(bucket, outputPath, batchID);
            writeResults(resultsByContext, outputURI);
            out.println(String.format("Wrote batch %s to %s", batchID, outputURI));
            // end for each aggregation context
        }
    }

    protected SparkSession.Builder withS3Config(SparkSession.Builder sparkBuilder, AWSClientConfig awsConfig) {
        return sparkBuilder.config("spark.hadoop.fs.s3a.access.key", awsConfig.getAwsAccessKey())
                .config("spark.hadoop.fs.s3a.secret.key", awsConfig.getAwsSecretKey())
                .config("spark.hadoop.fs.s3a.endpoint", awsConfig.getAwsEndpoint());
    }

    /**
     * Input data is expected to be divided into separate files per datatype. For
     * each datatype to be used in evaluations, read in the data, extract the
     * context ID from whatever column in the data contains the primary/foreign key
     * for the evaluation context, and then create a pair of context to row data.
     * This pair will subsequently be used to reorganize the data by context.
     * 
     * @param spark Active Spark Session
     * @return pairs of context value to data row for each datatype in the input
     */
    protected JavaPairRDD<Object, Row> readAllInputData(SparkSession spark) {
        JavaPairRDD<Object, Row> allData = null;

        for (String fact : facts) {

            String[] parts = fact.split(":");
            String filename = parts[0];
            String dataType = parts[0];
            if (parts.length > 1) {
                dataType = parts[1];
            }

            String contextColumn = getContextColumnForDataType(dataType);

            JavaPairRDD<Object, Row> contextIdRowPairs = readDataset(spark,
                    AWSClientHelpers.toS3Url(bucket, inputPath, filename), dataType, contextColumn);

            if (allData == null) {
                allData = contextIdRowPairs;
            } else {
                allData = allData.union(contextIdRowPairs);
            }
        }

        return allData;
    }

    /**
     * Given a dataType string, return the column name of the column that will be
     * used to aggregate data by evaluation context. For example, the Patient
     * context might be the person_id column in the input.
     * 
     * @param dataType DataType name
     * @return primary/foreign key column name (e.g. person_id)
     */
    protected String getContextColumnForDataType(String dataType) {
        // TODO - Does this need to be more sophisticated than a single column name for
        // every table that is used as input?
        return contextColumn;
    }

    /**
     * Read a single datatype's dataset. This assumes that data resides in an Amazon
     * compatible endpoint and is in parquet format.
     * 
     * @param spark         Active Spark Session
     * @param fileURI       The S3 URI pointing at the parquet file
     * @param dataType      The DataType string corresponding to the data being read
     * @param contextColumn The column name in the input data that corresponds to
     *                      the evaluation context
     * @return data mapped from context value to row content
     */
    protected JavaPairRDD<Object, Row> readDataset(SparkSession spark, String fileURI, String dataType,
            String contextColumn) {
        Dataset<Row> dataset = spark.read().parquet(fileURI).withColumn(SOURCE_FACT_IDX, functions.lit(dataType));

        return dataset.javaRDD().mapToPair(row -> {
            Object joinValue = row.getAs(contextColumn);
            return new Tuple2<>(joinValue, row);
        });
    }

    /**
     * Given a set of rows that are indexed by context value, reorganize the data so
     * that all rows related to the same context are grouped into a single pair.
     * 
     * @param allData rows mapped from context value to a single data row
     * @return rows grouped mapped from context value to a list of all data for that
     *         context
     */
    protected JavaPairRDD<Object, List<Row>> aggregateByContext(JavaPairRDD<Object, Row> allData) {
        // Regroup data by context ID so that all input data for the same
        // context is represented as a single key mapped to a list of rows

        JavaPairRDD<Object, List<Row>> combinedData;
        if (aggregateOnContext) {
            combinedData = allData.combineByKey(create -> {
                List<Row> dataRowList = new ArrayList<>();
                dataRowList.add(create);
                return dataRowList;
            }, (list, val) -> {
                list.add(val);
                return list;
            }, (list1, list2) -> {
                List<Row> dataRowList = new ArrayList<>(list1);
                dataRowList.addAll(list2);
                return dataRowList;
            });
        } else {
            // TODO: Not sure how much extra time is spent doing this needless work
            // that only serves to keep the "multirow" and "single row" usecases on the same
            // "java type".
            // If there's a big enough time sink here, then we may want to change
            // `combinedData` to be something super generic.
            combinedData = allData
                    .mapToPair((tuple2) -> new Tuple2<>(tuple2._1(), Collections.singletonList(tuple2._2())));
        }

        return combinedData;
    }

    /**
     * Evaluate the input CQL for a single context + data pair.
     *
     * @param rowsByContext Data for a single evaluation context
     * @return result of the evaluation of each specified expression mapped by
     *         context ID
     * @throws CqlLibraryDeserializationException if the CQL libraries cannot be
     *                                            loaded for any reason
     */
    protected Tuple2<Object, Map<String, Object>> evaluate(Tuple2<Object, List<Row>> rowsByContext) throws CqlLibraryDeserializationException {
        AmazonS3 s3client = AWSClientFactory.getInstance().createClient(AWSClientConfigFactory.fromEnvironment());

        CqlLibraryProvider libraryProvider = new S3CqlLibraryProvider(s3client, bucket, cqlPath);

        // TODO - replace with cohort shared translation component
        final CqlToElmTranslator translator = new CqlToElmTranslator();
        if (modelInfoPath != null) {
            AWSClientHelpers.processS3Objects(s3client, bucket, modelInfoPath, (osm,obj) -> {
                if( osm.getKey().endsWith(".xml") ) {
                    Reader r = new StringReader(obj);
                    translator.registerModelInfo(r);
                }
            });
        }
        TranslatingCqlLibraryProvider translatingLibraryProvider = new TranslatingCqlLibraryProvider(libraryProvider,
                translator);

        return evaluate(translatingLibraryProvider, rowsByContext);
    }



    /**
     * Evaluate the input CQL for a single context + data pair.
     * 
     * @param libraryProvider Library provider providing CQL/ELM content
     * @param rowsByContext Data for a single evaluation context
     * @return result of the evaluation of each specified expression mapped by
     *         context ID
     * @throws CqlLibraryDeserializationException if the CQL libraries cannot be
     *                                            loaded for any reason
     */
    protected Tuple2<Object, Map<String, Object>> evaluate(CqlLibraryProvider libraryProvider,
            Tuple2<Object, List<Row>> rowsByContext) {
        CqlTerminologyProvider termProvider = new UnsupportedTerminologyProvider();

        // Convert the Spark objects to the cohort Java model
        List<DataRow> datarows = rowsByContext._2().stream().map(getDataRowFactory()).collect(Collectors.toList());

        Map<String, List<Object>> dataByDataType = new HashMap<>();
        for (DataRow datarow : datarows) {
            String dataType = (String) datarow.getValue(SOURCE_FACT_IDX);
            List<Object> mappedRows = dataByDataType.computeIfAbsent(dataType, x -> new ArrayList<>());
            mappedRows.add(datarow);
        }

        DataRowRetrieveProvider retrieveProvider = new DataRowRetrieveProvider(dataByDataType, termProvider);
        CqlDataProvider dataProvider = new DataRowDataProvider(getDataRowClass(), retrieveProvider);

        CqlEvaluator evaluator = new CqlEvaluator().setLibraryProvider(libraryProvider)
                .setDataProvider(dataProvider).setTerminologyProvider(termProvider);

        CqlLibraryDescriptor topLevelLibrary = new CqlLibraryDescriptor().setLibraryId(libraryId)
                .setVersion(libraryVersion).setFormat(libraryFormat);

        // TODO - where do we get this data? CLI-style parameters? That doesn't work
        // very well if the values need to change on a per-context basis.
        Map<String, Object> parameters = new HashMap<>();

        CqlEvaluationResult result = evaluator.evaluate(topLevelLibrary, parameters, null, expressions, debug ? CqlDebug.DEBUG : CqlDebug.NONE);
        
        //TODO - map the expressionResults typesystem back into plain java objects. This would include converting the CQL
        //runtime types (Date, DateTime, Interval, Quantity, Ratio, Tuple, etc.) back to their Java equivalents (LocalDate, Instant, ???)
        //in addition to rendering any DataRow or list of DataRow objects as Spark datatypes.
        Map<String,Object> expressionResults = new HashMap<>();
        for( Map.Entry<String,Object> entry : result.getExpressionResults().entrySet() ) {
            expressionResults.put( entry.getKey(), typeConverter.toSparkType(entry.getValue()) );
        }
        
        return new Tuple2<>(rowsByContext._1(), expressionResults);
    }

    /**
     * Write the results of CQL evaluation to a given storage location.
     * 
     * @param resultsByContext CQL evaluation results mapped from context value to 
     * a map of define to define result.
     * @param outputURI URI pointing at the location where output data should be written.
     */
    protected void writeResults(JavaPairRDD<Object, Map<String, Object>> resultsByContext, String outputURI) {
        resultsByContext.saveAsTextFile(outputURI);
    }
    
    /**
     * Configuration flag that controls where the spark sql datetime Java 8 API
     * hooks are enabled. When used, java.time.LocalDate and java.time.Instant are
     * used in the program. When turned off java.sql.Date and java.sql.Timestamp
     * are used. The default Spark setting is false. The default setting for this
     * program is true.
     * 
     * @return true/false based on whether Java 8 APIs should be used.
     */
    protected boolean isUseJava8APIDatetime() {
        return true;
    }
    
    /**
     * Get the class object that will represent the individual data rows 
     * that will be created and used in the CQL runtime. This is important
     * as the CQL engine uses the package of model classes to map to 
     * data provider implementations. This method is provided to allow
     * subclasses to override the data row implementation as needed.
     * 
     * @return data row implementation class
     */
    protected Class<? extends DataRow> getDataRowClass() {
        return SparkDataRow.class;
    }
    
    /**
     * Get a function that will produce the data row classes
     * described by the getDataRowClass method. This allows subclasses
     * to override data row creation as needed. 
     * 
     * @return data row factory function
     */
    protected Function<Row, DataRow> getDataRowFactory() {
        return (row) -> new SparkDataRow( getSparkTypeConverter(), row);
    }
    
    /**
     * Get the SparkTypeConverter implementation that will be used
     * to do Spark to CQL and CQL to Spark type conversions. This method 
     * is provided so that subclasses can override the conversion logic
     * as needed.
     * 
     * @return spark type converter
     */
    protected SparkTypeConverter getSparkTypeConverter() {
        return this.typeConverter;
    }

    public static void main(String[] args) {
        SparkS3CqlEvaluator evaluator = new SparkS3CqlEvaluator();

        JCommander commander = JCommander.newBuilder().addObject(evaluator).build();
        commander.parse(args);

        if (evaluator.help) {
            commander.usage();
        } else {
            evaluator.run(System.out);
        }
    }
}