package com.ibm.cohort.cql.evaluation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class CqlTypedExpression { // extend off of here?
	private String expression;
	private String type;   // try enum -- users use shorter versions
	
	// maybe nullable flag
	
	private CqlTypedExpression() {
		
	}

	public CqlTypedExpression(String expression) {
		this(expression, null);
	}
	
	public CqlTypedExpression(String expression, String type) {
		this.expression = expression;
		this.type = type;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		CqlTypedExpression that = (CqlTypedExpression) o;

		return new EqualsBuilder()
				.append(expression, that.expression)
				.append(type, that.type)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(expression)
				.append(type)
				.toHashCode();
	}
}
