package com.flipkart.gap.usl.core.config.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.config.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import lombok.Data;

/**
 * Created by piyush.chhabra on 25/03/2019
 */
@Data
public class ApplicationConfiguration implements Serializable {
    private EventProcessorConfig eventProcessorConfig;
    private String dimensionPackage;
    private CacheConfig cacheConfig;
    private MongoConfig mongoConfig;
    private HbaseConfig hbaseConfig;

    @JsonCreator
    public ApplicationConfiguration(@JsonProperty("eventProcessorConfig") EventProcessorConfig eventProcessorConfig,
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
        this.validate();
    }

    private void validate() {

    }
}
