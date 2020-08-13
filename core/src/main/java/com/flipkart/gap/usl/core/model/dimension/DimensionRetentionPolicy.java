package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.retentionPolicyVisitor.RetentionPolicyVisitor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by amarjeet.singh on 03/10/16.
 */
@Getter
@Setter
public abstract class DimensionRetentionPolicy {
    private RetentionPolicyType retentionPolicyType;

    DimensionRetentionPolicy(RetentionPolicyType retentionPolicyType) {
        this.retentionPolicyType = retentionPolicyType;
    }

    public abstract <K> void visit(RetentionPolicyVisitor visitor, K elements);
}
