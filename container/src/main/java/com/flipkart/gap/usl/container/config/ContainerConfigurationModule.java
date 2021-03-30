package com.flipkart.gap.usl.container.config;

import com.flipkart.gap.usl.client.kafka.KafkaProducerClient;
import com.flipkart.gap.usl.container.eventIngestion.EventIngestionConfig;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ContainerConfigurationModule extends AbstractModule {
    private final ContainerConfig containerConfig;

    public ContainerConfigurationModule(ContainerConfig containerConfig) {
        this.containerConfig = containerConfig;
    }

    @Override
    protected void configure() {
        bind(EventIngestionConfig.class).annotatedWith(Names.named("eventIngestionConfig")).toInstance(containerConfig.getEventIngestionConfig());
        bind(KafkaProducerClient.class).annotatedWith(Names.named("ingestionKafkaClient")).toInstance(new KafkaProducerClient(containerConfig.getEventIngestionConfig().getKafkaIngestionConfig()));
        bind(String.class).annotatedWith(Names.named("healthCheckKey")).toInstance(containerConfig.getHealthCheckKey());
    }
}

