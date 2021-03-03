package com.flipkart.gap.usl.core.retentionPolicyVisitor;

import com.flipkart.gap.usl.core.helper.RetentionPolicyHelper;
import com.flipkart.gap.usl.core.model.dimension.DimensionCollection;
import com.flipkart.gap.usl.core.model.dimension.SizeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.SizeNTimeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.TimeBasedRetentionPolicy;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by vinay.lodha on 12/02/18.
 */
public class ListRetentionPolicyVisitor<K extends DimensionCollection.DimensionElement> implements RetentionPolicyVisitor<List<K>> {

    @Override
    public void visit(SizeNTimeBasedRetentionPolicy policy, List<K> elements) {
        if (elements != null) {
            sizeBasedRemoval(policy.getSizeLimit(), elements);
            timeBaseRemoval(policy.getTimeUnit(), policy.getDuration(), elements);
        }
    }

    @Override
    public void visit(SizeBasedRetentionPolicy policy, List<K> elements) {
        if (elements != null) {
            sizeBasedRemoval(policy.getLimit(), elements);
        }
    }

    @Override
    public void visit(TimeBasedRetentionPolicy policy, List<K> elements) {
        if (elements != null) {
            timeBaseRemoval(policy.getTimeUnit(), policy.getDuration(), elements);
        }
    }

    private void sizeBasedRemoval(int sizeLimit, List<K> elements) {
        Set<Integer> indexesToRemove = getIndexesToRemove(sizeLimit, elements);
        Iterator<K> iterator = elements.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            iterator.next();
            if (indexesToRemove.contains(index)) {
                iterator.remove();
            }
            index++;
        }
    }

    private void timeBaseRemoval(TimeUnit timeUnit, long duration, List<K> elements) {
        elements.removeIf(element -> RetentionPolicyHelper.isTimeLimitExceeded(timeUnit.toMillis(duration), element));
    }


    /**
     * Getting a set of Indexes to remove on LRU basis
     *
     * @param sizeLimit
     * @param elements
     * @return
     */
    private Set<Integer> getIndexesToRemove(int sizeLimit, List<K> elements) {
        if (elements.size() > sizeLimit) {
            int entriesToRemove = elements.size() - sizeLimit;
            List<Pair<Long, Integer>> sortedTimestampIndexPair = sortedTimestamp(elements);
            return sortedTimestampIndexPair.stream()
                    .limit(entriesToRemove)
                    .map(Pair::getValue)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    /**
     * sorting the Pair of Timestamp and Index in accessing order
     *
     * @param elements
     * @return
     */
    private List<Pair<Long, Integer>> sortedTimestamp(List<K> elements) {
        Iterator<K> iterator = elements.iterator();
        List<Pair<Long, Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            K next = iterator.next();
            pairs.add(Pair.of(next.getUpdated(), i));
        }
        return pairs.stream().sorted().collect(Collectors.toList());
    }
}
