package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.helper.RetentionPolicyHelper;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Created by sampanna.kahu on 20/03/17.
 */
@Slf4j
public abstract class DimensionMapBased<X, Z extends DimensionCollection.DimensionElement, E extends DimensionUpdateEvent, M extends DimensionMergeEvent> extends DimensionCollection<Map<X, Z>, E, M> {
    public DimensionMapBased(DimensionRetentionPolicy retentionPolicy, String entityId) {
        super(retentionPolicy, entityId);
    }

    @Override
    public void expire() {
        RetentionPolicyHelper.retainElements(getRetentionPolicy(), getElements());
    }

}
