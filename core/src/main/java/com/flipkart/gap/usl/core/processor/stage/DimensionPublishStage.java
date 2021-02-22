package com.flipkart.gap.usl.core.processor.stage;

import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.flipkart.gap.usl.core.store.dimension.kafka.KafkaPublisherDAO;
import com.flipkart.gap.usl.core.store.exception.DimensionPersistException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.stream.Collectors;

@Singleton
public class DimensionPublishStage extends ProcessingStage {

    @Inject
    private KafkaPublisherDAO kafkaPublisherDAO;

    @Override
    protected void process(ProcessingStageData processingStageData) throws StageProcessingException {
        try {
            kafkaPublisherDAO.bulkPublish(processingStageData.getDimensionMutateRequests().stream().map(DimensionMutateRequest::getDimension).collect(Collectors.toSet()));
        } catch (Exception e) {
            throw new StageProcessingException(e);
        }
    }
}
