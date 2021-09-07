/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

// TODO: Remove once unit tests are setup
public class CsvDatasetRetriever implements DatasetRetriever {

    private final SparkSession spark;

    public CsvDatasetRetriever(SparkSession spark) {
        this.spark = spark;
    }

    @Override
    public Dataset<Row> readDataset(String path) {
        return spark.read()
                // TODO: Parquet
                .option("header", true)
                .option("inferSchema", true)
                .csv(path);
    }

}