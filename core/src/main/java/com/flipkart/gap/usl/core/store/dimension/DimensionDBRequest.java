package com.flipkart.gap.usl.core.store.dimension;

import com.flipkart.gap.usl.core.model.dimension.Dimension;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class DimensionDBRequest {
    private Class<? extends Dimension> dimensionClass;
    private String entityId;
    private int version;
}
