package com.flipkart.gap.usl.core.config;

import com.flipkart.gap.usl.core.config.v2.KafkaIngestionApplicationConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;


public class KafkaIngestionConfigurationModule extends AbstractModule {
    private KafkaIngestionApplicationConfiguration configuration;
    private static Injector injector;

    public static synchronized Injector getInjector(KafkaIngestionApplicationConfiguration configuration) {
        if (injector == null) {
            injector = Guice.createInjector(new KafkaIngestionConfigurationModule(configuration));
        }
        return injector;
    }

    public KafkaIngestionConfigurationModule(KafkaIngestionApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(KafkaIngestionConfig.class).annotatedWith(Names.named("kafkaIngestionConfig")).toInstance(configuration.getKafkaIngestionConfig());
        bind(EventProcessorConfig.class).annotatedWith(Names.named("eventProcessorConfig")).toInstance(configuration.getEventProcessorConfig());
        bind(KafkaIngestionApplicationConfiguration.class).toInstance(configuration);
    }
}
