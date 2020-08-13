package com.flipkart.gap.usl.core.model.dimension;

import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by amarjeet.singh on 03/10/16.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DimensionSpecs {
    String name();

    Class<? extends DimensionUpdateEvent>[] updateEvents() default DummyUpdateEvent.class;

    Class<? extends DimensionMergeEvent> mergeEvent() default DummyMergeEvent.class;

    boolean enabled();
}
