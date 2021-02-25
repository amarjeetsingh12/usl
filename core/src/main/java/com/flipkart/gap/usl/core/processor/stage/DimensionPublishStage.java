package com.flipkart.gap.usl.core.processor.stage;

import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.flipkart.gap.usl.core.store.dimension.kafka.KafkaPublisherDAOImpl;
import com.flipkart.gap.usl.core.store.dimension.kafka.KafkaPublisherDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Singleton
@Slf4j
public class DimensionPublishStage extends ProcessingStage {

    @Inject
    private KafkaPublisherDao kafkaPublisherDao;

    @Override
    protected void process(ProcessingStageData processingStageData) throws StageProcessingException {

        log.error("Test message. DimensionPublishStage");
        try {
            kafkaPublisherDao.bulkPublish(processingStageData.getDimensionMutateRequests().stream().map(DimensionMutateRequest::getDimension).collect(Collectors.toSet()));
        } catch (Exception e) {
            throw new StageProcessingException(e);
        }
    }
}
