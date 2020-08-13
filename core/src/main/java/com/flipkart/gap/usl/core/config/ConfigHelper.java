package com.flipkart.gap.usl.core.config;

import com.flipkart.gap.usl.core.config.v2.ApplicationConfiguration;
import com.flipkart.gap.usl.core.constant.Constants;

import org.apache.log4j.MDC;

// TODO: Please implement methods for providing your own configs
public class ConfigHelper {

    public static ApplicationConfiguration getConfiguration() {
        return null;
    }

    public static boolean isLoadTest() {
        return MDC.get(Constants.X_PERF_TEST) != null && MDC.get(Constants.X_PERF_TEST).equals("true");
    }
}
