package com.flipkart.gap.usl.core.model;

import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode
public class DimensionMutateRequest implements Serializable {
    private EntityDimensionCompositeKey entityDimensionCompositeKey;
    private List<DimensionEvent> dimensionEvents;
    private Dimension dimension;

    public DimensionMutateRequest(EntityDimensionCompositeKey entityDimensionCompositeKey,List<DimensionEvent> dimensionEvents) {
        this.entityDimensionCompositeKey = entityDimensionCompositeKey;
        this.dimensionEvents = dimensionEvents;
    }
}
