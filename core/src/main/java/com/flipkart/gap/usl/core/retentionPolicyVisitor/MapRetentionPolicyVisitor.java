package com.flipkart.gap.usl.core.retentionPolicyVisitor;

/**
 * Created by vinay.lodha on 12/02/18.
 */

import com.flipkart.gap.usl.core.helper.RetentionPolicyHelper;
import com.flipkart.gap.usl.core.model.dimension.DimensionCollection;
import com.flipkart.gap.usl.core.model.dimension.SizeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.SizeNTimeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.TimeBasedRetentionPolicy;

import java.util.*;
import java.util.stream.Collectors;

public class MapRetentionPolicyVisitor<K, V extends DimensionCollection.DimensionElement> implements RetentionPolicyVisitor<Map<K, V>> {

    @Override
    public void visit(SizeNTimeBasedRetentionPolicy policy, Map<K, V> elements) {
        if (elements != null) {
            sizeBasedRemoval(policy.getSizeLimit(), elements);
            timeBaseRemoval(policy.getTimeUnit().toMillis(policy.getDuration()), elements);
        }
    }

    @Override
    public void visit(SizeBasedRetentionPolicy policy, Map<K, V> elements) {
        if (elements != null) {
            sizeBasedRemoval(policy.getLimit(), elements);
        }
    }

    @Override
    public void visit(TimeBasedRetentionPolicy policy, Map<K, V> elements) {
        if (elements != null) {
            timeBaseRemoval(policy.getLimitInMilliseconds(), elements);
        }
    }

    private void timeBaseRemoval(long limitInMilliseconds, Map<K, V> elements) {
        Iterator<Map.Entry<K, V>> iterator = elements.entrySet().iterator();
        while (iterator.hasNext()) {
            V value = iterator.next().getValue();
            if (RetentionPolicyHelper.isTimeLimitExceeded(limitInMilliseconds, value)) {
                iterator.remove();
            }
        }
    }

    private void sizeBasedRemoval(int sizeLimit, Map<K, V> elements) {
        List<Map.Entry<K, V>> sortedEntries = sortedEntries(elements);
        List<K> keysToRemove = getKeysToRemove(sizeLimit, sortedEntries);
        removeEntries(keysToRemove, elements);
    }

    private List<K> getKeysToRemove(int sizeLimit, List<Map.Entry<K, V>> sortedEntries) {
        int entriesToRemoves = sortedEntries.size() - sizeLimit;
        List<K> keysToRemove = new ArrayList<>();
        for (Map.Entry<K, V> entry : sortedEntries) {
            if (keysToRemove.size() < entriesToRemoves) {
                // size based remove
                keysToRemove.add(entry.getKey());
            }
        }
        return keysToRemove;
    }

    private void removeEntries(List<K> keysToRemove, Map<K, V> elements) {
        keysToRemove.forEach(elements::remove);
    }

    private List<Map.Entry<K, V>> sortedEntries(Map<K, V> elements) {
        return elements.entrySet().stream()
                .sorted(Comparator.comparingLong(v -> v.getValue().getUpdated()))
                .collect(Collectors.toList());
    }

}
