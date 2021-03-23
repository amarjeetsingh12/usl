package com.flipkart.gap.usl.core.helper;

import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Created by amarjeet.singh on 05/01/17.
 */
public class SparkHelper {
    private static void setLogId() {
        if (MDC.get(Constants.LOG_ID) == null || MDC.get(Constants.LOG_ID).isEmpty()) {
            synchronized (SparkHelper.class) {
                if (MDC.get(Constants.LOG_ID) == null || MDC.get(Constants.LOG_ID).isEmpty()) {
                    MDC.put(Constants.LOG_ID, UUID.randomUUID().toString());
                }
            }
        }
    }

    public static void bootstrap() {
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry();
        SparkHelper.setLogId();
    }
}
