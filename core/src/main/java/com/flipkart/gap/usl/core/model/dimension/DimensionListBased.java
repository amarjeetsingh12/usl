package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.helper.RetentionPolicyHelper;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by amarjeet.singh on 18/11/16.
 */
@Slf4j
public abstract class DimensionListBased<Z extends DimensionCollection.DimensionElement, E extends DimensionUpdateEvent, M extends DimensionMergeEvent> extends DimensionCollection<List<Z>, E, M> {

    public DimensionListBased(DimensionRetentionPolicy retentionPolicy, String entityId) {
        super(retentionPolicy, entityId);
    }

    @Override
    public void expire() {
        RetentionPolicyHelper.retainElements(getRetentionPolicy(), getElements());
    }
}
