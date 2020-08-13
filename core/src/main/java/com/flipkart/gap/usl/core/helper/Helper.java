package com.flipkart.gap.usl.core.helper;

import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Created by amarjeet.singh on 05/01/17.
 */
public class Helper {
    private static void setLogId() {
        if (MDC.get(Constants.LOG_ID) == null || MDC.get(Constants.LOG_ID).isEmpty()) {
            synchronized (Helper.class) {
                if (MDC.get(Constants.LOG_ID) == null || MDC.get(Constants.LOG_ID).isEmpty()) {
                    MDC.put(Constants.LOG_ID, UUID.randomUUID().toString());
                }
            }
        }
    }

    public static void initializeClients() {
        Helper.setLogId();
        // Initialize your DB/RPC Clients
    }

    public static void initializeRegisteries(String client, String environment) {
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry(client);
        // Initialise DocumentRegistry, if any
    }
}
