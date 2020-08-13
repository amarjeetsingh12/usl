package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * CompositeKey to be used to group events based on entityId,dimensionName and eventClass
 */
@Getter
@Setter
public class DimensionEntityCompositeKey implements Serializable {
    private String entityId;
    private String dimensionName;
    private Class<? extends DimensionEvent> eventClass;

    public DimensionEntityCompositeKey(String entityId, String dimensionName, Class<? extends DimensionEvent> eventClass) {
        this.entityId = entityId;
        this.dimensionName = dimensionName;
        this.eventClass = eventClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DimensionEntityCompositeKey)) return false;

        DimensionEntityCompositeKey that = (DimensionEntityCompositeKey) o;

        return entityId != null ? entityId.equals(that.entityId) : that.entityId == null && (dimensionName != null ? dimensionName.equals(that.dimensionName) : that.dimensionName == null && (eventClass != null ? eventClass.equals(that.eventClass) : that.eventClass == null));

    }

    @Override
    public int hashCode() {
        int result = entityId != null ? entityId.hashCode() : 0;
        result = 31 * result + (dimensionName != null ? dimensionName.hashCode() : 0);
        result = 31 * result + (eventClass != null ? eventClass.hashCode() : 0);
        return result;
    }
}
