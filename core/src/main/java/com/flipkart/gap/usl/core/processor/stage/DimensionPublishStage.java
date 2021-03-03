package com.flipkart.gap.usl.core.processor.stage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.flipkart.gap.usl.core.store.dimension.kafka.KafkaPublisherDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class DimensionPublishStage extends ProcessingStage {

    @Inject
    private KafkaPublisherDao kafkaPublisherDao;

    @Override
    protected void process(ProcessingStageData processingStageData) throws StageProcessingException {
        try {

            final Set<Dimension> dimensionSet = processingStageData.getDimensionMutateRequests().stream().map(DimensionMutateRequest::getDimension).collect(Collectors.toSet());

            Iterator<Dimension> dimensionIterator = dimensionSet.iterator();
            while (dimensionIterator.hasNext()) {
                Dimension dimension = dimensionIterator.next();
                kafkaPublisherDao.publish(dimension.getDimensionSpecs().name(),
                        ObjectMapperFactory.getMapper().writeValueAsBytes(dimension));
            }

        } catch (Exception e) {
            throw new StageProcessingException(e);
        }
    }

}
