package com.flipkart.gap.usl.core.retentionPolicyVisitor;

import com.flipkart.gap.usl.core.model.dimension.SizeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.SizeNTimeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.TimeBasedRetentionPolicy;

/**
 * Created by vinay.lodha on 12/02/18.
 */
public interface RetentionPolicyVisitor<T> {
    void visit(SizeNTimeBasedRetentionPolicy policy, T elements);
    void visit(SizeBasedRetentionPolicy policy, T elements);
    void visit(TimeBasedRetentionPolicy policy, T elements);
}
