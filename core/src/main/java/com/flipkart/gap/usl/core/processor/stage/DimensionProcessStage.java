package com.flipkart.gap.usl.core.processor.stage;

import com.flipkart.gap.usl.core.processor.BatchProcessor;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DimensionProcessStage extends ProcessingStage {
    @Inject
    private BatchProcessor batchProcessor;

    @Override
    protected void process(ProcessingStageData request) throws StageProcessingException {
        try {
            if (request.getDimensionMutateRequests() != null && !request.getDimensionMutateRequests().isEmpty()) {
                request.getDimensionMutateRequests().forEach(dimensionMutateRequest
                        -> batchProcessor.process(dimensionMutateRequest.getDimension(), dimensionMutateRequest.getDimensionEvents()));
            }
        } catch (Throwable throwable) {
            throw new StageProcessingException(throwable);
        }
    }
}
