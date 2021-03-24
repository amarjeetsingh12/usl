package com.flipkart.gap.usl.core.processor.stage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flipkart.gap.usl.core.config.v2.ApplicationConfiguration;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.processor.stage.model.KafkaProducerRecord;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.flipkart.gap.usl.core.store.dimension.kafka.KafkaPublisherDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class DimensionPublishStage extends ProcessingStage {

    private final String DIMENSION_NAME_ENTITY_ID_DELIMITER = "##";

    @Inject
    private KafkaPublisherDao kafkaPublisherDao;

    @Inject
    ApplicationConfiguration configuration;

    @Override
    protected void process(ProcessingStageData processingStageData) throws StageProcessingException {
        try {

            final Set<Dimension> dimensionSet = processingStageData.getDimensionMutateRequests().stream().map(DimensionMutateRequest::getDimension).collect(Collectors.toSet());

            List<KafkaProducerRecord> producerRecordList = dimensionSet.stream()
                    .filter(dimension -> configuration.getDimensionsToBePublished().contains(dimension.getDimensionSpecs().name()))
                    .map(dimension -> {
                        try {
                            return createProducerRecord(dimension);
                        } catch (JsonProcessingException e) {
                            throw new StageProcessingException(e);
                        }
                    })
                    .collect(Collectors.toList());

            if (!producerRecordList.isEmpty())
                kafkaPublisherDao.sendRecords(producerRecordList);

        } catch (Exception e) {
            throw new StageProcessingException(e);
        }
    }

    private KafkaProducerRecord createProducerRecord(Dimension dimension) throws JsonProcessingException {

        String dimensionName = dimension.getDimensionSpecs().name();
        String partitionKey = dimensionName + DIMENSION_NAME_ENTITY_ID_DELIMITER + dimension.getEntityId();
        byte[] value = ObjectMapperFactory.getMapper().writeValueAsBytes(dimension);

        /*
        Its published to the topic with the name which is same as dimension name
         */
        return new KafkaProducerRecord(dimensionName, partitionKey, value);
    }


}
