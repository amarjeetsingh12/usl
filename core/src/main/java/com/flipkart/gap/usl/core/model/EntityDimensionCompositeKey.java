package com.flipkart.gap.usl.core.model;

import com.flipkart.gap.usl.core.model.dimension.DimensionSpec;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class EntityDimensionCompositeKey implements Serializable {
    private String entityId;
    private DimensionSpec dimensionSpec;
}
