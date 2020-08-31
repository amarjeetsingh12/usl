package com.flipkart.gap.usl.container.utils;

import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import java.util.concurrent.TimeUnit;

public class JmxReporterMetricRegistry {
    private static JmxReporterMetricRegistry instance;
    private final MetricRegistry metricRegistry;

    private JmxReporterMetricRegistry() {
        metricRegistry = new MetricRegistry();
        String metrics = "metrics";
        JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .inDomain(metrics)
                .build();
        jmxReporter.start();
    }

    public static void initialiseJmxMetricRegistry() {
        if (instance == null) {
            synchronized (JmxReporterMetricRegistry.class) {
                instance = new JmxReporterMetricRegistry();
            }
        }
    }

    public static JmxReporterMetricRegistry getInstance() {
        return instance;
    }

    public void markMeter(String name) {
        instance.metricRegistry.meter(String.format("com.flipkart.gap.usl.meter.%s", name)).mark();
    }

    public void markMeter(Class<?> callerClass, long count, String... names) {
        instance.metricRegistry.meter(MetricRegistry.name(callerClass, names)).mark(count);
    }

    public void markMeter(Class<?> callerClass, String... names) {
        instance.metricRegistry.meter(MetricRegistry.name(callerClass, names)).mark(1);
    }

}
