package com.flipkart.gap.usl.core.config.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.config.CacheConfig;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.config.ZKConfig;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Created by piyush.chhabra on 25/03/2019
 */
@Data
public class ApplicationConfiguration implements Serializable {
    private EventProcessorConfig eventProcessorConfig;
    private String dimensionPackage;
    private ZKConfig zkConfig;
    private CacheConfig cacheConfig;
    private String[] whitelistedClients;
    private List<Map<String, String>> productCompareBasketVerticalsMaxProductCounts;

    @JsonCreator
    public ApplicationConfiguration(@JsonProperty("eventProcessorConfig") EventProcessorConfig eventProcessorConfig,
                                    @JsonProperty("dimensionPackage") String dimensionPackage,
                                    @JsonProperty("zkConfig") ZKConfig zkConfig,
                                    @JsonProperty("cacheConfig") CacheConfig cacheConfig,
                                    @JsonProperty("whitelistedClients") String[] whitelistedClients) {
        this.eventProcessorConfig = eventProcessorConfig;
        this.dimensionPackage = dimensionPackage;
        this.zkConfig = zkConfig;
        this.cacheConfig = cacheConfig;
        this.whitelistedClients = whitelistedClients;
        this.validate();
    }

    private void validate() {

    }
}
