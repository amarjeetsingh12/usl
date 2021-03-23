package com.flipkart.gap.usl.container.eventIngestion;

import com.flipkart.gap.usl.client.kafka.ProducerConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by ashish.khatkar on 19/02/18.
 */
@Getter
@Setter
@NoArgsConstructor
public class EventIngestionConfig {
    private String kafkaTopicName;
    private ProducerConfig kafkaIngestionConfig;
}
