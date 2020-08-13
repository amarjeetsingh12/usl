package com.flipkart.gap.usl.container.utils;

import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.external.ExternalEventSchema;
import com.flipkart.gap.usl.core.processor.SyncEventProcessor;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.google.inject.Inject;

import java.util.Optional;

/**
 * Created by ashish.khatkar on 16/10/18.
 */
public class SyncEventProcessorService {
    @Inject
    private SyncEventProcessor syncEventProcessor;

    public ProcessingStageData processEventAndGetStageData(ExternalEventSchema externalEventSchema) throws Throwable {
        return syncEventProcessor.processEvent(externalEventSchema);
    }

    public <T extends Dimension> T getDimensionFromStageData(ProcessingStageData processingStageData, Class<T> dimensionClass) {
        Optional<DimensionMutateRequest> lastUpdatedDMR = processingStageData.getDimensionMutateRequests().stream()
                .filter(dimensionMutateRequest -> dimensionMutateRequest.getEntityDimensionCompositeKey().getDimensionSpec().getDimensionClass() == dimensionClass)
                .min((o1, o2) -> (int) (o2.getDimension().getUpdated() - o1.getDimension().getUpdated()));
        return lastUpdatedDMR.map(dimensionMutateRequest -> (T) dimensionMutateRequest.getDimension()).orElse(null);
    }
}
