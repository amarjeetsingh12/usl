package com.flipkart.gap.usl.container;

import com.fasterxml.jackson.annotation.JsonProperty;
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

}