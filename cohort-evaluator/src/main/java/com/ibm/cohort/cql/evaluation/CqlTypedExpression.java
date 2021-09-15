package com.ibm.cohort.cql.evaluation;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CqlTypedExpression {
    // TODO: Figure out how to use these annotations
    @NotNull
    private String expression;
    @NotNull
    private CqlSparkTypeEnum returnType;
    
    private CqlTypedExpression() {
        
    }

    public CqlTypedExpression(String expression, CqlSparkTypeEnum returnType) {
        this.expression = expression;
        this.returnType = returnType;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public CqlSparkTypeEnum getReturnType() {
        return returnType;
    }

    public void setReturnType(CqlSparkTypeEnum returnType) {
        this.returnType = returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CqlTypedExpression that = (CqlTypedExpression) o;

        return new EqualsBuilder()
                .append(expression, that.expression)
                .append(returnType, that.returnType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(expression)
                .append(returnType)
                .toHashCode();
    }
}
