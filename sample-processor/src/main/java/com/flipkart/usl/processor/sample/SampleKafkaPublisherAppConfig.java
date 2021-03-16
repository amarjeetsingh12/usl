package com.flipkart.usl.processor.sample;

import com.flipkart.gap.usl.core.config.v2.ExternalKafkaApplicationConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SampleKafkaPublisherAppConfig {
    private ExternalKafkaApplicationConfiguration coreConfig;
}
