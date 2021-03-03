package com.flipkart.usl.processor.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.flipkart.gap.usl.core.config.ConfigHelper;
import com.flipkart.gap.usl.core.config.v2.ApplicationConfiguration;
import com.flipkart.gap.usl.core.helper.SparkHelper;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.processor.EventStreamProcessor;
import com.flipkart.gap.usl.core.processor.exception.ProcessingException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class SampleProcessorApp {
    private static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static void main(String[] args) throws ProcessingException, IOException {

        System.out.println("Starting application at " + System.currentTimeMillis());
        try {
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry();
        // Initialise Document Registries if any.
        SparkHelper.bootstrap();
        SampleAppConfig sampleAppConfig = yamlMapper.readValue(new File(args[0]), SampleAppConfig.class);
        Injector injector = Guice.createInjector(new com.flipkart.gap.usl.core.config.ConfigurationModule(sampleAppConfig.getCoreConfig()));
        EventStreamProcessor eventStreamProcessor = injector.getInstance(EventStreamProcessor.class);
        log.info("EventStreamConsumer created");
        eventStreamProcessor.process();
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
