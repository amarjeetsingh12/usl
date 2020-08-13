package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.retentionPolicyVisitor.RetentionPolicyVisitor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by amarjeet.singh on 10/11/16.
 */
@Getter
@Setter
public class SizeBasedRetentionPolicy extends DimensionRetentionPolicy {
    private int limit;

    public SizeBasedRetentionPolicy(int limit) {
        super(RetentionPolicyType.SIZE_BASED);
        this.limit = limit;
    }

    @Override
    public <K> void visit(RetentionPolicyVisitor visitor, K elements) {
        visitor.visit(this, elements);
    }
}
