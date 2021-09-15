package com.ibm.cohort.cql.evaluation;


import com.fasterxml.jackson.annotation.JsonProperty;

public enum CqlSparkTypeEnum {
	@JsonProperty("string")
	STRING,
	@JsonProperty("binary")
	BINARY,
	@JsonProperty("boolean")
	BOOLEAN
}
