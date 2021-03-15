package com.flipkart.gap.usl.core.config;

import com.flipkart.gap.usl.core.config.v2.ExternalKafkaApplicationConfiguration;
import com.flipkart.gap.usl.core.store.dimension.kafka.ExternalKafkaPublisherDAOImpl;
import com.flipkart.gap.usl.core.store.dimension.kafka.KafkaPublisherDao;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;


public class ExternalKafkaConfigurationModule extends AbstractModule {
    private ExternalKafkaApplicationConfiguration configuration;
    private static Injector injector;

    public static synchronized Injector getInjector(ExternalKafkaApplicationConfiguration configuration) {
        if (injector == null) {
            injector = Guice.createInjector(new ExternalKafkaConfigurationModule(configuration));
        }
        return injector;
    }

    public ExternalKafkaConfigurationModule(ExternalKafkaApplicationConfiguration configuration) {
        this.configuration = configuration;
    }


    @Override
    protected void configure() {
        bind(EventProcessorConfig.class).annotatedWith(Names.named("externalKafkaConfig")).toInstance(configuration.getExternalKafkaConfig());
        bind(EventProcessorConfig.class).annotatedWith(Names.named("eventProcessorConfig")).toInstance(configuration.getEventProcessorConfig());
        bind(KafkaPublisherDao.class).to(ExternalKafkaPublisherDAOImpl.class);
        bind(ExternalKafkaApplicationConfiguration.class).toInstance(configuration);
    }
}
