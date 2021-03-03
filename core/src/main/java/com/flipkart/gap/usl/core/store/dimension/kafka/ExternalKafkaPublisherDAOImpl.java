package com.flipkart.gap.usl.core.store.dimension.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.CompressionType;

import java.util.Properties;

@Singleton
@Slf4j
public class ExternalKafkaPublisherDAOImpl implements KafkaPublisherDao{

    private final Integer KAFKA_MESSAGE_BATCH_SIZE_MAX = 100;
    private final Integer KAFKA_LINGER_SIZE_MS = 10;

    @Inject
    @Named("externalKafkaConfig")
    private EventProcessorConfig externalKafkaConfig;

    private Producer<String, byte[]> producer;

    @Inject
    public void init() {
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, externalKafkaConfig.getKafkaBrokerConnection());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, KAFKA_MESSAGE_BATCH_SIZE_MAX);
        props.put(ProducerConfig.LINGER_MS_CONFIG, KAFKA_LINGER_SIZE_MS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.GZIP.name);

        try {
            producer = new org.apache.kafka.clients.producer.KafkaProducer<String, byte[]>(props);
        } catch (Exception e) {
            log.error("Exception making producer: {}", ExceptionUtils.getMessage(e), e);
        }
    }

    public void publish(String topic, byte[] record) throws Exception {
        producer.send(createProducerRecord(topic, record)).get();
    }

    private ProducerRecord<String,byte[]> createProducerRecord(String topic, byte[] record) throws JsonProcessingException {

        return new ProducerRecord<>(topic,record);
    }


}
