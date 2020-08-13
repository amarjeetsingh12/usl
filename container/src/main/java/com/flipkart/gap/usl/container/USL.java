package com.flipkart.gap.usl.container;

import com.flipkart.gap.usl.app.execution.SystemProperties;
import com.flipkart.gap.usl.app.execution.USLClient;
import com.flipkart.gap.usl.app.execution.USLEnvironment;
import com.flipkart.gap.usl.container.guiceconfig.USLContainerFactory;
import com.flipkart.gap.usl.core.exception.InvalidClientException;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class USL {

    public static void main(String[] args) throws Exception {
        USLClient uslClient = SystemProperties.getUslClient();
        USLEnvironment env = SystemProperties.getUslEnvironment();
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry(uslClient.getId());
        // TODO: Initialise registries / configs as needed.

        USLContainer uslContainer = null;
        try {
            uslContainer = USLContainerFactory.getContainer(uslClient, env);
        } catch (InvalidClientException e) {
            log.error("Invalid ClientId", e);
            System.exit(-1);
        }
        uslContainer.run(args);
    }
}
