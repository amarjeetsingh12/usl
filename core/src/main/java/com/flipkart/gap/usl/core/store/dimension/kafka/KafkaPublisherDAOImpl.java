package com.flipkart.gap.usl.core.store.dimension.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

@Singleton
@Slf4j
public class KafkaPublisherDAOImpl implements KafkaPublisherDao{

    private final Integer KAFKA_MESSAGE_BATCH_SIZE_MAX = 100;
    private final Integer KAFKA_LINGER_SIZE_MS = 10;

    @Inject
    @Named("eventProcessorConfig")
    private EventProcessorConfig eventProcessorConfig;
//    @Getter
//    private ProducerConfig producerConfig;
    private Producer<String, byte[]> producer;

    @Inject
    public void init() {
//        this.producerConfig = producerConfig;
        Properties props = new Properties();

        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, eventProcessorConfig.getKafkaBrokerConnection());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BATCH_SIZE_CONFIG, KAFKA_MESSAGE_BATCH_SIZE_MAX);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, KAFKA_LINGER_SIZE_MS);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.GZIP.name);

        try {
            producer = new org.apache.kafka.clients.producer.KafkaProducer<String, byte[]>(props);
        } catch (Exception e) {
            log.error("Exception making producer: {}", ExceptionUtils.getMessage(e), e);
        }
    }


    private void sendEvent(Dimension dimension) throws Exception {
        log.error("Dimension: {}", dimension.getEntityId());
        try {
            producer.send(createProducerRecord(dimension)).get();
        } catch (Exception e) {
            log.error("Exception is publishing event: {}", e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private ProducerRecord<String,byte[]> createProducerRecord(Dimension dimension) throws JsonProcessingException {

        return new ProducerRecord<>(dimension.getDimensionSpecs().name(),
                ObjectMapperFactory.getMapper().writeValueAsBytes(dimension)
        );
    }

    @Override
    public void bulkPublish(Set<Dimension> dimensions) throws Exception {

        log.error("Test message. KafkaPublisherDAOImpl");
        Iterator<Dimension> dimensionIterator = dimensions.iterator();
        while (dimensionIterator.hasNext()) {
            sendEvent(dimensionIterator.next());
        }

    }


}
