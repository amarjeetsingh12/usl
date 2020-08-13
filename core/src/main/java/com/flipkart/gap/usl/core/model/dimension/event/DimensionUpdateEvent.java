package com.flipkart.gap.usl.core.model.dimension.event;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by amarjeet.singh on 03/10/16.
 */
@Getter
@Setter
public abstract class DimensionUpdateEvent extends DimensionEvent {
    public DimensionUpdateEvent() {
        super(DimensionEventType.UPDATE);
    }
}
