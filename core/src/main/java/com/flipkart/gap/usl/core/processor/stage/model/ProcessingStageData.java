package com.flipkart.gap.usl.core.processor.stage.model;

import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class ProcessingStageData implements Serializable {
    private List<DimensionMutateRequest> dimensionMutateRequests;
}
