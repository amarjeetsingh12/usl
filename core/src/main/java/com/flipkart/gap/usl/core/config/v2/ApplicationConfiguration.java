package com.flipkart.gap.usl.core.config.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.config.CacheConfig;
//import com.flipkart.gap.usl.core.config.CoreConfig;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.config.HbaseConfig;
import com.flipkart.gap.usl.core.config.MongoConfig;
import com.flipkart.gap.usl.core.config.resilience.ResilienceConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by piyush.chhabra on 25/03/2019
 */
@Data
public class ApplicationConfiguration implements Serializable {

    private EventProcessorConfig externalKafkaConfig;
    private EventProcessorConfig eventProcessorConfig;
    private Map<String,ResilienceConfig> applicationResilienceConfig;
    private String dimensionPackage;
    private CacheConfig cacheConfig;
    private MongoConfig mongoConfig;
    private HbaseConfig hbaseConfig;

    @JsonCreator
    public ApplicationConfiguration(
            @JsonProperty("eventProcessorConfig") EventProcessorConfig eventProcessorConfig,
            @JsonProperty("applicationResilienceConfig") Map<String,ResilienceConfig> applicationResilienceConfig,
            @JsonProperty("dimensionPackage") String dimensionPackage,
            @JsonProperty("cacheConfig") CacheConfig cacheConfig,
            @JsonProperty("mongoConfig") MongoConfig mongoConfig,
            @JsonProperty("hbaseConfig") HbaseConfig hbaseConfig
    ) {
        this.eventProcessorConfig = eventProcessorConfig;
        this.dimensionPackage = dimensionPackage;
        this.cacheConfig = cacheConfig;
        this.mongoConfig = mongoConfig;
        this.hbaseConfig = hbaseConfig;
        this.applicationResilienceConfig = applicationResilienceConfig;
        this.validate();
    }

    private void validate() {

    }
}
