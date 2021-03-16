package com.flipkart.gap.usl.core.client;

import com.flipkart.gap.usl.core.config.InternalEventProcessorConfig;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


@Singleton
@Slf4j
public class InternalEventKafkaClient {
    @Inject
    @Named("internalEventProcessorConfig")
    private InternalEventProcessorConfig eventProcessorConfig;
    private Producer<String, byte[]> producer;
    private KafkaConsumer<String, byte[]> consumer;
    private Map<String, Integer> partitionCountMap;

    public InternalEventKafkaClient() {
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
        partitionCountMap = new HashMap<>();
        for (String topicName: eventProcessorConfig.getTopicNames()) {
            partitionCountMap.put(topicName, producer.partitionsFor(topicName).size());
        }

    }

    public Map<String, Integer> getPartitionCountMap() {
        return partitionCountMap;
    }

    public int getPartitionCount(String topicName) {
        return partitionCountMap.get(topicName);
    }

    public Map<Integer, Long> getPartitionOffsets(String topicName) {
        List<TopicPartition> topicPartitions = new ArrayList<>();
        for (int i = 0; i < partitionCountMap.get(topicName); i++) {
            topicPartitions.add(new TopicPartition(topicName, i));
        }
        Map<TopicPartition, Long> offsetMap = consumer.beginningOffsets(topicPartitions);
        return offsetMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().partition(), Map.Entry::getValue));
    }

    public void tearDown() {
        producer.close();
    }

}
