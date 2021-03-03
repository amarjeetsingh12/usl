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
public class TimeBasedRetentionPolicy extends DimensionRetentionPolicy {
    private long duration;
    private TimeUnit timeUnit;

    public TimeBasedRetentionPolicy(TimeUnit timeUnit, long duration) {
        super(RetentionPolicyType.TIME_BASED);
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public TimeBasedRetentionPolicy(long duration) {
        this(TimeUnit.DAYS, duration);
    }

    @Override
    public <K> void visit(RetentionPolicyVisitor visitor, K elements) {
        visitor.visit(this, elements);
    }
}
