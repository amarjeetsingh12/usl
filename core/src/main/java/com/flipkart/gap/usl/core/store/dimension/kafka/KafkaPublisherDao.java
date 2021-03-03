package com.flipkart.gap.usl.core.store.dimension.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

public interface KafkaPublisherDao {

    void publish(String topicName, byte[] record) throws Exception;

}
