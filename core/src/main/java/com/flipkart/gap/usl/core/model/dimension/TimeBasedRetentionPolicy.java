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
    private long limitInMilliseconds;

    public TimeBasedRetentionPolicy(long limitInMilliseconds) {
        super(RetentionPolicyType.TIME_BASED);
        this.limitInMilliseconds = limitInMilliseconds;
    }

    @Override
    public <K> void visit(RetentionPolicyVisitor visitor, K elements) {
        visitor.visit(this, elements);
    }
}
