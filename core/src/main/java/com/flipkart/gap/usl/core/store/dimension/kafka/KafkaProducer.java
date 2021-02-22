package com.flipkart.gap.usl.core.store.dimension.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.util.*;

/**
 * Created by vinay.lodha on 20/06/17.
 */
@Slf4j
@Singleton
public class KafkaProducer {

    @Inject
    @Named("eventProcessorConfig")
    private EventProcessorConfig eventProcessorConfig;
    @Getter
    private ProducerConfig producerConfig;
    private Producer<String, byte[]> producer;

    public KafkaProducer(ProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
        int maxBytesInBuffer = 1024 * 1024 * 100;
        Properties props = new Properties();
        props.put("bootstrap.servers", eventProcessorConfig.getKafkaBrokerConnection());
        props.put("key.deserializer", ByteArrayDeserializer.class);
        props.put("value.deserializer", ByteArrayDeserializer.class);
        props.put("group.id", eventProcessorConfig.getKafkaConfig().getGroupId());
        props.put("auto.offset.reset", eventProcessorConfig.getKafkaConfig().getAutoOffsetReset());
        props.put("enable.auto.commit", eventProcessorConfig.getKafkaConfig().isEnableAutoCommit());
        props.put("fetch.max.wait.ms", eventProcessorConfig.getKafkaConfig().getFetchMaxWait());
        props.put("fetch.min.bytes", eventProcessorConfig.getKafkaConfig().getFetchMinBytes());
        props.put("heartbeat.interval.ms", eventProcessorConfig.getKafkaConfig().getHeartBeatIntervalMS());
        props.put("session.timeout.ms", eventProcessorConfig.getKafkaConfig().getSessionTimeoutMS());
        props.put("request.timeout.ms", eventProcessorConfig.getKafkaConfig().getRequestTimeoutMS());

        producer = new org.apache.kafka.clients.producer.KafkaProducer<String, byte[]>(props);
    }

}

