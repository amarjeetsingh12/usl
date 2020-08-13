package com.flipkart.gap.usl.client.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by vinay.lodha on 20/06/17.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class KafkaEventRequest {
    private ProducerEvent producerEvent;
    private String topicName;
    private String key;

    public KafkaEventRequest(ProducerEvent producerEvent, String topicName) {
        this(producerEvent, topicName, null);
    }
}
