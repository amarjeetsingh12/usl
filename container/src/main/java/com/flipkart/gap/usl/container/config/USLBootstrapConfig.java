package com.flipkart.gap.usl.container.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.client.kafka.ProducerConfig;
import com.flipkart.gap.usl.container.eventIngestion.EventIngestionConfig;
import com.flipkart.gap.usl.core.config.v2.ApplicationConfiguration;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ajaysingh on 05/10/16.
 */
@Getter
@Setter
public class USLBootstrapConfig extends Configuration {
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;
    private ApplicationConfiguration coreConfig;
    private EventIngestionConfig eventIngestionConfig;
    private String healthCheckKey = "default";
}