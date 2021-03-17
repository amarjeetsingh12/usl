package com.flipkart.gap.usl.core.config;

import com.flipkart.gap.usl.core.config.v2.ExternalKafkaApplicationConfiguration;
import com.flipkart.gap.usl.core.store.dimension.kafka.KafkaPublisherDao;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;


public class KafkaIngestionConfigurationModule extends AbstractModule {
    private ExternalKafkaApplicationConfiguration configuration;
    private static Injector injector;

    public static synchronized Injector getInjector(ExternalKafkaApplicationConfiguration configuration) {
        if (injector == null) {
            injector = Guice.createInjector(new KafkaIngestionConfigurationModule(configuration));
        }
        return injector;
    }

    public KafkaIngestionConfigurationModule(ExternalKafkaApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(KafkaIngestionConfig.class).annotatedWith(Names.named("kafkaIngestionConfig")).toInstance(configuration.getKafkaIngestionConfig());
        bind(EventProcessorConfig.class).annotatedWith(Names.named("eventProcessorConfig")).toInstance(configuration.getEventProcessorConfig());
        bind(ExternalKafkaApplicationConfiguration.class).toInstance(configuration);
    }
}
