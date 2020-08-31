package com.flipkart.gap.usl.core.model.dimension.event;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * Created by amarjeet.singh on 13/11/16.
 */
@Getter
@Setter
public abstract class DimensionEvent implements Serializable {
    private long created;
    @NotEmpty
    private String entityId;
    private DimensionEventType eventType;

    public DimensionEvent(DimensionEventType eventType) {
        this.created = System.currentTimeMillis();
        this.eventType = eventType;
    }

    public DimensionEvent() {
        created = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DimensionEvent{");
        sb.append("created=").append(created);
        sb.append(", entityId='").append(entityId).append('\'');
        sb.append(", eventType=").append(eventType);
        sb.append('}');
        return sb.toString();
    }
}
