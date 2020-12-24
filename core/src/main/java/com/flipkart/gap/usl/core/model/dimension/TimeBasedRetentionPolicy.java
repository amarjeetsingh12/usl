package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.retentionPolicyVisitor.RetentionPolicyVisitor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by amarjeet.singh on 10/11/16.
 */
@Getter
@Setter
public class TimeBasedRetentionPolicy extends DimensionRetentionPolicy {
    private int limitInMinutes;

    public TimeBasedRetentionPolicy(int limitInMinutes) {
        super(RetentionPolicyType.TIME_BASED);
        this.limitInMinutes = limitInMinutes;
    }

    @Override
    public <K> void visit(RetentionPolicyVisitor visitor, K elements) {
        visitor.visit(this, elements);
    }
}
