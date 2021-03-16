package com.flipkart.gap.usl.core.store.dimension.kafka;

import com.flipkart.gap.usl.core.config.ExternalKafkaConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.record.CompressionType;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
@Slf4j
public class ExternalKafkaPublisherDAOImpl extends KafkaPublisherDao{

    @Inject
    @Named("externalKafkaConfig")
    private ExternalKafkaConfig externalKafkaConfig;

    @Inject
    public void init() {
        props = new Properties();

        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, externalKafkaConfig.getKafkaBrokerConnection());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG, externalKafkaConfig.getRetry());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BATCH_SIZE_CONFIG, externalKafkaConfig.getBatchSize());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, externalKafkaConfig.getLingerTimeInMs());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, externalKafkaConfig.getRequestTimeout());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.MAX_BLOCK_MS_CONFIG, externalKafkaConfig.getMaxBlockMS());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, externalKafkaConfig.getMaxIdleTime());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.GZIP.name);


        try {
            producers = new LinkedBlockingQueue<>(externalKafkaConfig.getProducersCount());
            for (int i = 0; i < externalKafkaConfig.getProducersCount(); i++) {
                producers.add(new KafkaProducer<>(props));
            }
            if (externalKafkaConfig.getExecutorServicePoolSize() != 0) {
                executorServicePool = Executors.newFixedThreadPool(externalKafkaConfig.getExecutorServicePoolSize());
            }
        } catch (Exception e) {
            log.error("Exception making producer: {}", ExceptionUtils.getMessage(e), e);
        }
    }


}
