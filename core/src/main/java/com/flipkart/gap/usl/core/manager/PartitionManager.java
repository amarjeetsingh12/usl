package com.flipkart.gap.usl.core.manager;

import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.store.dimension.kafka.KafkaClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class PartitionManager {

    @Inject
    private KafkaClient kafkaClient;

    @Inject
    @Named("eventProcessorConfig")
    private EventProcessorConfig eventProcessorConfig;

    private Map<String, Integer> partitionCountMap;

    @Inject
    public void init() {
        partitionCountMap = new HashMap<>();
        for (String topicName: eventProcessorConfig.getTopicNames()) {
            partitionCountMap.put(topicName, kafkaClient.getPartitionInfo(topicName).size());
        }
    }

    public int getPartitionCount(String topicName) {
        return partitionCountMap.get(topicName);
    }

    public Map<String, Integer> getPartitionCountMap() {
        return partitionCountMap;
    }

    public Map<Integer, Long> getEarliestOffsets(String topicName) {
        List<TopicPartition> topicPartitions = new ArrayList<>();
        int partitionCount = getPartitionCount(topicName);
        for (int i = 0; i < partitionCount; i++) {
            topicPartitions.add(new TopicPartition(topicName, i));
        }

        Map<TopicPartition, Long> offsetMap = kafkaClient.getEarliestOffsets(topicPartitions);

        return offsetMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().partition(), Map.Entry::getValue));

    }


}
