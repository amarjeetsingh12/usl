package com.flipkart.gap.usl.client.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flipkart.gap.usl.client.utils.ObjectMapperFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.record.CompressionType;

import java.util.*;

/**
 * Created by vinay.lodha on 20/06/17.
 */
@Slf4j
public class KafkaProducerClient {
    @Getter
    private ProducerConfig producerConfig;
    private Producer<String, byte[]>[] producers;
    private Random random = new Random();

    public KafkaProducerClient(ProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
        int maxBytesInBuffer = 1024 * 1024 * 100;
        Properties props = new Properties();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerConfig.getBrokerConnectionString());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG, producerConfig.getRetry());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BATCH_SIZE_CONFIG, producerConfig.getBatchSize());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, producerConfig.getLingerTimeInMs());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, producerConfig.getRequestTimeout());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.MAX_BLOCK_MS_CONFIG, producerConfig.getMaxBlockMS());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, producerConfig.getMaxIdleTime());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BUFFER_MEMORY_CONFIG, maxBytesInBuffer / producerConfig.getProducersCount());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.GZIP.name);
        producers = new Producer[producerConfig.getProducersCount()];
        for (int i = 0; i < producerConfig.getProducersCount(); i++) {
            producers[i] = new KafkaProducer<>(props);
        }
    }

    private ProducerRecord<String, byte[]> createProducerRecord(final KafkaEventRequest kafkaEventRequest) throws JsonProcessingException {
        return new ProducerRecord<>(kafkaEventRequest.getTopicName(), kafkaEventRequest.getKey(), ObjectMapperFactory.getObjectMapper().writeValueAsBytes(kafkaEventRequest.getProducerEvent()));
    }

    public void sendEvent(final KafkaEventRequest kafkaEventRequest, Callback callback) throws JsonProcessingException {
        getProducer().send(createProducerRecord(kafkaEventRequest), callback);
    }

    public void sendEventSync(final KafkaEventRequest kafkaEventRequest) throws Exception {
        getProducer().send(createProducerRecord(kafkaEventRequest)).get();
    }

    public void sendEvent(final KafkaEventRequest kafkaEventRequest) throws Exception {
        sendEvent(kafkaEventRequest, (metadata, exception) -> {
            if (null != exception) {
                log.error("Error sending event for {}", kafkaEventRequest.toString(), exception);
            }
        });
    }

    private Producer<String, byte[]> getProducer() {
        return this.producers[random.nextInt(producerConfig.getProducersCount())];
    }

    public void tearDown() {
        for (Producer producer : producers) {
            producer.flush();
            producer.close();
        }
    }
}

