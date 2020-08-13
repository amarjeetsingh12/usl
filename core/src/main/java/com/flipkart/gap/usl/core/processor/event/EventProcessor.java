package com.flipkart.gap.usl.core.processor.event;

import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpec;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.registry.DimensionRegistry;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by amarjeet.singh on 15/11/16.
 */
@Getter
@Setter
abstract class EventProcessor<T extends DimensionEvent> {
    @Inject
    private DimensionRegistry dimensionRegistry;

    public abstract <D extends Dimension> D process(D dimension, List<T> dimensionEvents, DimensionSpec dimensionSpec) throws Throwable;
}
