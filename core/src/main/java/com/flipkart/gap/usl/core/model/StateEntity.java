package com.flipkart.gap.usl.core.model;

import com.flipkart.gap.usl.core.model.dimension.Dimension;

import java.util.Map;

/**
 * Created by amarjeet.singh on 03/10/16.
 */
public abstract class StateEntity {
    private long created;
    private long updated;
    private String entityId;
    private Map<String, Dimension> dimensions;

    public StateEntity(String entityId) {
        this.entityId = entityId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public Map<String, Dimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
