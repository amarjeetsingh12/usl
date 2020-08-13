package com.flipkart.gap.usl.core.model;

import com.flipkart.gap.usl.core.model.dimension.DimensionSpec;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import lombok.*;

import java.io.Serializable;

/**
 * Created by amarjeet.singh on 14/11/16.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class InternalEventMeta implements Comparable<InternalEventMeta>, Serializable {
    private DimensionEvent dimensionEvent;
    private DimensionSpec dimensionSpec;

    @Override
    public int compareTo(InternalEventMeta o) {
        return Long.compare(dimensionEvent.getCreated(), o.getDimensionEvent().getCreated());
    }
}
