package com.flipkart.gap.usl.core.config.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.config.ExternalEventConfig;
import com.flipkart.gap.usl.core.config.InternalEventProcessorConfig;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExternalKafkaApplicationConfiguration implements Serializable {

    private ExternalEventConfig externalEventConfig;
    private InternalEventProcessorConfig internalEventProcessorConfig;

    @JsonCreator
    public ExternalKafkaApplicationConfiguration(
            @JsonProperty("externalEventConfig") ExternalEventConfig externalEventConfig,
            @JsonProperty("internalEventProcessorConfig") InternalEventProcessorConfig internalEventProcessorConfig
    ) {
        this.externalEventConfig = externalEventConfig;
        this.internalEventProcessorConfig = internalEventProcessorConfig;
        this.validate();
    }

    private void validate() {

    }
}
