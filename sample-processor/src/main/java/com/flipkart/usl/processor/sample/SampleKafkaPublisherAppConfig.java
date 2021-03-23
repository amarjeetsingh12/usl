package com.flipkart.usl.processor.sample;

import com.flipkart.gap.usl.core.config.v2.KafkaIngestionApplicationConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SampleKafkaPublisherAppConfig {
    private KafkaIngestionApplicationConfiguration coreConfig;
}
