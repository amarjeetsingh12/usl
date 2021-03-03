package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.retentionPolicyVisitor.RetentionPolicyVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * Created by amarjeet.singh on 10/11/16.
 */
@Getter
@Setter
public final class SizeNTimeBasedRetentionPolicy extends DimensionRetentionPolicy {
    private long duration;
    private TimeUnit timeUnit;
    private int sizeLimit;

    public SizeNTimeBasedRetentionPolicy(TimeUnit timeUnit, long duration, int sizeLimit) {
        super(RetentionPolicyType.SIZE_N_TIME_BASED);
        this.duration = duration;
        this.timeUnit = timeUnit;
        this.sizeLimit = sizeLimit;
    }

    public SizeNTimeBasedRetentionPolicy(long days, int sizeLimit) {
        super(RetentionPolicyType.SIZE_N_TIME_BASED);
        this.duration = days;
        this.timeUnit = TimeUnit.DAYS;
        this.sizeLimit = sizeLimit;
    }


    @Override
    public <K> void visit(RetentionPolicyVisitor visitor, K elements) {
        visitor.visit(this, elements);
    }

}
