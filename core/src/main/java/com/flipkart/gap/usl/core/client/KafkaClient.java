package com.flipkart.gap.usl.core.client;

import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import kafka.api.OffsetRequest;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.*;

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


    public KafkaClient() {
    }

    @Inject
    public void init() {
        Properties props = new Properties();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, eventProcessorConfig.getKafkaBrokerConnection());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        producer = new KafkaProducer<>(props);
    }

    public int getPartitionCount() {
        return producer.partitionsFor(eventProcessorConfig.getTopicName()).size();
    }

    public Map<Integer, Long> getPartitionOffsets() throws Exception {
        for (String broker : eventProcessorConfig.getKafkaBrokerConnection().split(",")) {
            String parts[] = broker.split(":");
            try {
                return findPartitionOffsets(parts[0], Integer.parseInt(parts[1]));
            } catch (Throwable t) {
                log.error("Error finding offsets from host {}", String.join(parts[0], parts[1]), t);
            }
        }
        throw new Exception("Unable to find offsets from any of the brokers");
    }

    private Map<Integer, Long> findPartitionOffsets(String host, int port) throws Exception {
        int soTimeout = 100000;
        int bufferSize = 64 * 1024;
        String clientName = "usl";
        String topicName = eventProcessorConfig.getTopicName();
        Map<Integer, Long> partitionMap = new HashMap<>();
        SimpleConsumer consumer = new SimpleConsumer(host,
                port,
                soTimeout,
                bufferSize, clientName);
        List<String> topics = new ArrayList<>();
        topics.add(topicName);
        TopicMetadataRequest req = new TopicMetadataRequest(topics);
        kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);
        List<kafka.javaapi.TopicMetadata> data3 = resp.topicsMetadata();
        for (kafka.javaapi.TopicMetadata item : data3) {
            for (kafka.javaapi.PartitionMetadata part : item.partitionsMetadata()) {
                int partitionId = part.partitionId();
                SimpleConsumer myConsumer = new SimpleConsumer(part.leader().host(), part.leader().port(),
                        soTimeout, bufferSize, clientName);
                try {
                    TopicAndPartition topicAndPartition = new TopicAndPartition(topicName, partitionId);
                    Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<>();
                    requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(
                            OffsetRequest.EarliestTime(), 1));
                    kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(
                            requestInfo, kafka.api.OffsetRequest.CurrentVersion(),
                            clientName);
                    OffsetResponse response = myConsumer.getOffsetsBefore(request);
                    long[] offsets = response.offsets(topicName, partitionId);
                    partitionMap.put(partitionId, offsets[0]);
                } finally {
                    myConsumer.close();
                }
            }
        }
        return partitionMap;
    }

    public void tearDown() {
        producer.close();
    }

}
