package com.flipkart.gap.usl.core.model.dimension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Created by amarjeet.singh on 03/10/16.
 */
@Getter
@Setter
@Slf4j
public abstract class DimensionCollection<A, E extends DimensionUpdateEvent, M extends DimensionMergeEvent> extends Dimension<E, M> {
    private A elements;
    @JsonIgnore
    private DimensionRetentionPolicy retentionPolicy;

    public DimensionCollection(DimensionRetentionPolicy retentionPolicy,String entityId) {
        super(entityId);
        this.retentionPolicy = retentionPolicy;
    }

    @Getter
    @Setter
    @SuppressWarnings("NullableProblems")
    public abstract static class DimensionElement implements Serializable {
        private long created;
        private long updated;

        public DimensionElement() {
            this.created = System.currentTimeMillis();
            this.updated = System.currentTimeMillis();
        }
    }

}
