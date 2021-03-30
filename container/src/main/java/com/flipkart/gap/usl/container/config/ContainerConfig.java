package com.flipkart.gap.usl.container.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.container.eventIngestion.EventIngestionConfig;
import com.flipkart.gap.usl.core.config.v2.ApplicationConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContainerConfig {
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;
    private ApplicationConfiguration coreConfig;
    private EventIngestionConfig eventIngestionConfig;
    private String healthCheckKey = "default";
}
