package com.ibm.cohort.cql.spark.data;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;

public interface SparkOutputColumnEncoder {
	String getColumnName(CqlEvaluationRequest request, String defineName);
}
