package com.flipkart.gap.usl.core.config;

import com.flipkart.gap.usl.core.config.resilience.ApplicationResilienceConfig;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.flipkart.gap.usl.core.store.dimension.hbase.HBaseDimensionStoreDAO;
import com.flipkart.gap.usl.core.store.event.EventMappingStore;
import com.flipkart.gap.usl.core.store.event.EventTypeStore;
import com.flipkart.gap.usl.core.store.event.mongo.MongoEventMappingStore;
import com.flipkart.gap.usl.core.store.event.mongo.MongoEventStore;
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

    public static synchronized Injector getInjector(ApplicationConfiguration configuration) {
        if (injector == null) {
            injector = Guice.createInjector(new ConfigurationModule(configuration));
        }
        return injector;
    }

    public ConfigurationModule(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }


    @Override
    protected void configure() {
        bind(MongoConfig.class).annotatedWith(Names.named("event.mongoConfig")).toInstance(configuration.getMongoConfig());
        bind(EventMappingStore.class).to(MongoEventMappingStore.class);
        bind(EventTypeStore.class).to(MongoEventStore.class);
        bind(HbaseConfig.class).annotatedWith(Names.named("hbaseConfig")).toInstance(configuration.getHbaseConfig());
        bind(DimensionStoreDAO.class).to(HBaseDimensionStoreDAO.class);
        bind(CacheConfig.class).annotatedWith(Names.named("cacheConfig")).toInstance(configuration.getCacheConfig());
        bind(EventProcessorConfig.class).annotatedWith(Names.named("eventProcessorConfig")).toInstance(configuration.getEventProcessorConfig());
        bind(ApplicationResilienceConfig.class).toInstance(configuration.getApplicationResilienceConfig());
        bind(ApplicationConfiguration.class).toInstance(configuration);
        bind(String.class).annotatedWith(Names.named("dimensionPackage")).toInstance(configuration.getDimensionPackage());
        bind(SyncEventProcessor.class).to(SyncEventProcessorImpl.class);
    }
}
