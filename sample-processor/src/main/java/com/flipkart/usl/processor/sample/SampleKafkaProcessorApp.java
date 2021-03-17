package com.flipkart.usl.processor.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.flipkart.gap.usl.core.config.KafkaIngestionConfigurationModule;
import com.flipkart.gap.usl.core.helper.SparkHelper;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.processor.ExternalKafkaPublisher;
import com.flipkart.gap.usl.core.processor.exception.ProcessingException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class SampleKafkaProcessorApp {
    private static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static void main(String[] args) throws ProcessingException, IOException {

        JmxReporterMetricRegistry.initialiseJmxMetricRegistry();
        // Initialise Document Registries if any.
        SparkHelper.bootstrap();
        SampleKafkaPublisherAppConfig sampleAppConfig = yamlMapper.readValue(new File(args[0]), SampleKafkaPublisherAppConfig.class);
        Injector injector = Guice.createInjector(new KafkaIngestionConfigurationModule(sampleAppConfig.getCoreConfig()));
        ExternalKafkaPublisher eventStreamProcessor = injector.getInstance(ExternalKafkaPublisher.class);
        log.info("EventStreamConsumer created");
        eventStreamProcessor.process();
    }
}
