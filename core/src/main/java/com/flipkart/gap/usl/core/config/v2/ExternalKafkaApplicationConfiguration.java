package com.flipkart.gap.usl.core.config.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by piyush.chhabra on 25/03/2019
 */
@Data
public class ExternalKafkaApplicationConfiguration implements Serializable {

    private EventProcessorConfig externalKafkaConfig;
    private EventProcessorConfig eventProcessorConfig;

    @JsonCreator
    public ExternalKafkaApplicationConfiguration(
            @JsonProperty("externalKafkaConfig") EventProcessorConfig externalKafkaConfig,
            @JsonProperty("eventProcessorConfig") EventProcessorConfig eventProcessorConfig
    ) {
        this.externalKafkaConfig = externalKafkaConfig;
        this.eventProcessorConfig = eventProcessorConfig;
        this.validate();
    }

    private void validate() {

    }
}
