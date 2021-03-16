package com.flipkart.gap.usl.core.config.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.config.ExternalKafkaConfig;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExternalKafkaApplicationConfiguration implements Serializable {

    private EventProcessorConfig eventProcessorConfig;
    private ExternalKafkaConfig externalKafkaConfig;


    @JsonCreator
    public ExternalKafkaApplicationConfiguration(
            @JsonProperty("externalKafkaConfig") ExternalKafkaConfig externalKafkaConfig,
            @JsonProperty("eventProcessorConfig") EventProcessorConfig eventProcessorConfig
    ) {
        this.externalKafkaConfig = externalKafkaConfig;
        this.eventProcessorConfig = eventProcessorConfig;
        this.validate();
    }

    private void validate() {

    }
}
