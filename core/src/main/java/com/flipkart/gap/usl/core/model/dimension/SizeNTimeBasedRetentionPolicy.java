package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.retentionPolicyVisitor.RetentionPolicyVisitor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by amarjeet.singh on 10/11/16.
 */
@Getter
@Setter
public final class SizeNTimeBasedRetentionPolicy extends DimensionRetentionPolicy {
    private int limitInMinutes;
    private int sizeLimit;

    public SizeNTimeBasedRetentionPolicy(int limitInMinutes, int sizeLimit) {
        super(RetentionPolicyType.SIZE_N_TIME_BASED);
        this.limitInMinutes = limitInMinutes;
        this.sizeLimit = sizeLimit;
    }


    @Override
    public <K> void visit(RetentionPolicyVisitor visitor, K elements) {
        visitor.visit(this, elements);
    }

}
