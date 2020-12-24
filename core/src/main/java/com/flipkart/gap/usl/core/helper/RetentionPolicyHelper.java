package com.flipkart.gap.usl.core.helper;

import com.flipkart.gap.usl.core.model.dimension.DimensionCollection;
import com.flipkart.gap.usl.core.model.dimension.DimensionRetentionPolicy;
import com.flipkart.gap.usl.core.retentionPolicyVisitor.ListRetentionPolicyVisitor;
import com.flipkart.gap.usl.core.retentionPolicyVisitor.MapRetentionPolicyVisitor;
import com.flipkart.gap.usl.core.retentionPolicyVisitor.RetentionPolicyVisitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by vinay.lodha on 12/02/18.
 */
public class RetentionPolicyHelper {

    private static RetentionPolicyVisitor mapRetentionPolicyVisitor = new MapRetentionPolicyVisitor();
    private static RetentionPolicyVisitor listRetentionPolicyVisitor = new ListRetentionPolicyVisitor();

    public static <K,V extends DimensionCollection.DimensionElement> void retainElements(DimensionRetentionPolicy policy, Map<K, V> elements) {
        policy.visit(mapRetentionPolicyVisitor, elements);
    }

    public static <K extends DimensionCollection.DimensionElement> void retainElements(DimensionRetentionPolicy policy, List<K> list) {
        policy.visit(listRetentionPolicyVisitor, list);
    }

    public static <V extends DimensionCollection.DimensionElement> boolean isTimeLimitExceeded(int minutes, V element) {
        return element.getUpdated() + TimeUnit.MINUTES.toMillis(minutes) < System.currentTimeMillis();
    }
}
