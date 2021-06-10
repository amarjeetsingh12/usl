package com.flipkart.gap.usl.core.model.dimension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.exception.DimensionMergeException;
import com.flipkart.gap.usl.core.exception.DimensionUpdateException;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by amarjeet.singh on 03/10/16.
 */
@Getter
@Setter
public abstract class Dimension<DE extends DimensionUpdateEvent, ME extends DimensionMergeEvent> implements Serializable {
    private long created;
    private long updated;
    private int version;
    private String entityId;

    @JsonCreator
    public Dimension(@JsonProperty("entityId") String entityId) {
        long timestamp = System.currentTimeMillis();
        this.created = timestamp;
        this.updated = timestamp;
        this.version = Constants.DIMENSION_VERSION;
        this.entityId = entityId;
    }

    public final void processUpdate(List<DE> dimensionUpdateEvents) throws DimensionUpdateException {
        this.updated = System.currentTimeMillis();
        this.update(dimensionUpdateEvents);
        this.expire();
    }

    protected abstract void update(List<DE> events) throws DimensionUpdateException;

    public abstract void expire();

    public final void processMerge(Dimension existingDimension,ME mergeEvent) throws DimensionMergeException {
        this.merge(existingDimension, mergeEvent);
        this.expire();
    }

    public abstract void merge(Dimension existingDimension, ME mergeEvents) throws DimensionMergeException;

    @JsonIgnore
    public DimensionSpecs getDimensionSpecs() {
        return this.getClass().getAnnotation(DimensionSpecs.class);
    }
}
