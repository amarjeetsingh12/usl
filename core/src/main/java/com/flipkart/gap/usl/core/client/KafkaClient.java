package com.flipkart.gap.usl.core.client;

import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by amarjeet.singh on 13/10/16.
 */
@Singleton
@Slf4j
public class KafkaClient {
    @Inject
    @Named("eventProcessorConfig")
    private EventProcessorConfig eventProcessorConfig;
    private Producer<String, byte[]> producer;
    private KafkaConsumer<String, byte[]> consumer;
    private int producerCount;

    public KafkaClient() {
        System.out.println();
    }

    @Inject
    public void init() {
        Properties props = new Properties();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, eventProcessorConfig.getKafkaBrokerConnection());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");

        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, eventProcessorConfig.getKafkaBrokerConnection());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, eventProcessorConfig.getKafkaConfig().getGroupId());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumer = new KafkaConsumer<>(consumerProperties);
        producer = new KafkaProducer<>(props);
        producerCount = producer.partitionsFor(eventProcessorConfig.getTopicName()).size();
    }

    public int getPartitionCount() {
        return producerCount;
    }

    public Map<Integer, Long> getPartitionOffsets() throws Exception {
        List<TopicPartition> topicPartitions = new ArrayList<>();
        for (int i = 0; i < producerCount; i++) {
            topicPartitions.add(new TopicPartition(eventProcessorConfig.getTopicName(), i));
        }
        Map<TopicPartition, Long> offsetMap = consumer.beginningOffsets(topicPartitions);
        return offsetMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().partition(), Map.Entry::getValue));
    }

    public void tearDown() {
        producer.close();
    }

}
