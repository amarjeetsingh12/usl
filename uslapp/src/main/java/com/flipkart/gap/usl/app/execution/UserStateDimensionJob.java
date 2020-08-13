package com.flipkart.gap.usl.app.execution;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.flipkart.gap.usl.core.config.ConfigHelper;
import com.flipkart.gap.usl.core.helper.Helper;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.processor.EventStreamProcessor;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by amarjeet.singh on 21/11/16.
 */
@Slf4j
public class UserStateDimensionJob {

    public static void main(String[] args) throws Exception {
        USLClient clientId = SystemProperties.getUslClient();
        USLEnvironment env = SystemProperties.getUslEnvironment();
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry(clientId.getId());
        // Initialise Document Registries if any.
        Helper.initializeClients();
        Injector injector = Guice.createInjector(new com.flipkart.gap.usl.core.config.ConfigurationModule(ConfigHelper.getConfiguration(), clientId.getId(), env.getId()));
        EventStreamProcessor eventStreamProcessor = injector.getInstance(EventStreamProcessor.class);
        log.info("EventStreamConsumer created");
        eventStreamProcessor.process();
    }
}
