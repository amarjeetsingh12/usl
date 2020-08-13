package com.flipkart.gap.usl.core.config;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import com.flipkart.gap.usl.core.config.v2.ApplicationConfiguration;
import com.flipkart.gap.usl.core.processor.SyncEventProcessor;
import com.flipkart.gap.usl.core.processor.impl.SyncEventProcessorImpl;

/**
 * Created by amarjeet.singh on 21/11/16.
 */

public class ConfigurationModule extends AbstractModule {
    private ApplicationConfiguration configuration;
    private static Injector injector;
    private static String clientId;
    private static String env;

    public static synchronized Injector getInjector(String client, String env) {
        if (injector == null) {
            injector = Guice.createInjector(new ConfigurationModule(ConfigHelper.getConfiguration(), client, env));
        }
        return injector;
    }

    public ConfigurationModule(ApplicationConfiguration configuration, String client, String environment) {
        clientId = client;
        env = environment;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(ZKConfig.class).annotatedWith(Names.named("zkConfig")).toInstance(configuration.getZkConfig());
        bind(CacheConfig.class).annotatedWith(Names.named("cacheConfig")).toInstance(configuration.getCacheConfig());
        bind(EventProcessorConfig.class).annotatedWith(Names.named("eventProcessorConfig")).toInstance(configuration.getEventProcessorConfig());
        bind(String.class).annotatedWith(Names.named("dimensionPackage")).toInstance(configuration.getDimensionPackage());
        bind(String.class).annotatedWith(Names.named("clientId")).toInstance(clientId);
        bind(String.class).annotatedWith(Names.named("env")).toInstance(env);
        bind(SyncEventProcessor.class).to(SyncEventProcessorImpl.class);

        // Add mappings to your DAO Impls and DB Configs.
    }
}
