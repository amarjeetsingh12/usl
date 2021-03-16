package com.flipkart.gap.usl.core.store.dimension.kafka;

import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Singleton
@Slf4j
public class KafkaClient {
    @Inject
    @Named("eventProcessorConfig")
    private EventProcessorConfig eventProcessorConfig;
    private KafkaConsumer<String, byte[]> consumer;

    public KafkaClient() {
        System.out.println();
    }

    @Inject
    public void init() {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, eventProcessorConfig.getKafkaBrokerConnection());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, eventProcessorConfig.getKafkaConfig().getGroupId());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumer = new KafkaConsumer<>(consumerProperties);
    }

    public List<PartitionInfo> getPartitionInfo(String topicName) {
        return consumer.partitionsFor(topicName);
    }

    public Map<TopicPartition, Long> getPartitionOffsets(List<TopicPartition> topicPartitions) {
        return consumer.beginningOffsets(topicPartitions);
    }

}
