package com.flipkart.gap.usl.core.config.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.config.KafkaIngestionConfig;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExternalKafkaApplicationConfiguration implements Serializable {

    private EventProcessorConfig eventProcessorConfig;
    private KafkaIngestionConfig kafkaIngestionConfig;

    @JsonCreator
    public ExternalKafkaApplicationConfiguration(
            @JsonProperty("externalKafkaConfig") KafkaIngestionConfig kafkaIngestionConfig,
            @JsonProperty("eventProcessorConfig") EventProcessorConfig eventProcessorConfig) {
        this.kafkaIngestionConfig = kafkaIngestionConfig;
        this.eventProcessorConfig = eventProcessorConfig;
    }


}
