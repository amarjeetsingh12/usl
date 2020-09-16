package com.flipkart.gap.usl.container.config;

import com.flipkart.gap.usl.client.kafka.KafkaProducerClient;
import com.flipkart.gap.usl.container.eventIngestion.EventIngestionConfig;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ContainerConfigurationModule extends AbstractModule {
    private final USLBootstrapConfig configuration;


    public ContainerConfigurationModule(USLBootstrapConfig configuration) {
        this.configuration = configuration;

    }

    @Override
    protected void configure() {
        bind(EventIngestionConfig.class).annotatedWith(Names.named("eventIngestionConfig")).toInstance(configuration.getEventIngestionConfig());
        bind(KafkaProducerClient.class).annotatedWith(Names.named("ingestionKafkaClient")).toInstance(new KafkaProducerClient(configuration.getEventIngestionConfig().getKafkaIngestionConfig()));
        bind(String.class).annotatedWith(Names.named("healthCheckKey")).toInstance(configuration.getHealthCheckKey());
    }
}
