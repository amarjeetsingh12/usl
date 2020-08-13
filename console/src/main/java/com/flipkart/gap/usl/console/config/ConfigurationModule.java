package com.flipkart.gap.usl.console.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import com.flipkart.gap.usl.core.config.CacheConfig;

/**
 * Created by ankesh.maheshwari on 25/10/16.
 */
public class ConfigurationModule extends AbstractModule {
    private ApplicationConfiguration configuration;

    public ConfigurationModule(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        //TODO: Bind DAO Impls and configs
        bind(CacheConfig.class).annotatedWith(Names.named("cacheConfig")).toInstance(configuration.getCacheConfig());
    }
}
