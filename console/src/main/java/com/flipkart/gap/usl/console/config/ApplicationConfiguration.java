package com.flipkart.gap.usl.console.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.config.CacheConfig;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.Getter;

/**
 * Created by ankesh.maheshwari on 13/10/16.
 */
@Getter
public class ApplicationConfiguration extends Configuration {
    private CacheConfig cacheConfig;
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;
}
