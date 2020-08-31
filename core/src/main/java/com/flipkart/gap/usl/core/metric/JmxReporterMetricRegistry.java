package com.flipkart.gap.usl.core.metric;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.flipkart.gap.usl.core.constant.Constants;

import java.util.concurrent.TimeUnit;

/**
 * Created by ankesh.maheshwari on 23/11/16.
 */
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

    public static MetricRegistry getMetricRegistry() {
        return instance.metricRegistry;
    }

    public static JmxReporterMetricRegistry getInstance() {
        return instance;
    }

    public Timer getEventParsingTimer() {
        return instance.metricRegistry.timer("EVENT_PARSING_TIMER");
    }

    public Timer getDimensionProcessTimer() {
        return instance.metricRegistry.timer(("DIMENSION_PROCESS_TIMER"));
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

    public void markValidationFailureMeter(String name) {
        instance.metricRegistry.meter(Constants.Metrics.VALIDATION_FAILURE_PATTERN).mark();
        instance.metricRegistry.meter(String.format(Constants.Metrics.VALIDATION_FAILURE_NAME_PATTERN, name)).mark();
    }

    public void markEntityIdMissing(String sourceEventId, String eventType) {
        instance.metricRegistry.meter(Constants.Metrics.ENTITY_ID_MISSING).mark();
        instance.metricRegistry.meter(String.format(Constants.Metrics.ENTITY_ID_MISSING_NAME_PATTERN, sourceEventId, eventType)).mark();
    }

    public void markDimensionUpdateSuccessMeter(String name) {
        instance.metricRegistry.meter(String.format(Constants.Metrics.DIMENSION_UPDATE_SUCCESS_NAME_PATTERN, name)).mark();
        instance.metricRegistry.meter(Constants.Metrics.DIMENSION_UPDATE_SUCCESS_PATTERN).mark();
    }

    public void markDimensionUpdateFailureMeter(String name) {
        instance.metricRegistry.meter(String.format(Constants.Metrics.DIMENSION_UPDATE_FAILURE_NAME_PATTERN, name)).mark();
        instance.metricRegistry.meter(Constants.Metrics.DIMENSION_UPDATE_FAILURE_PATTERN).mark();
    }

    public void markDimensionMergeSuccessMeter(String name) {
        instance.metricRegistry.meter(String.format(Constants.Metrics.DIMENSION_MERGE_SUCCESS_NAME_PATTERN, name)).mark();
        instance.metricRegistry.meter(Constants.Metrics.DIMENSION_MERGE_SUCCESS_PATTERN).mark();
    }

    public void markDimensionMergeFailureMeter(String name) {
        instance.metricRegistry.meter(String.format(Constants.Metrics.DIMENSION_MERGE_FAILURE_NAME_PATTERN, name)).mark();
        instance.metricRegistry.meter(Constants.Metrics.DIMENSION_MERGE_FAILURE_PATTERN).mark();
    }

    public void markExternalEventNotRegistered(String name) {
        instance.markMeter(String.format(Constants.Metrics.EXTERNAL_EVENT_NOT_REGISTERED_NAME_PATTERN, name));
        instance.markMeter(Constants.Metrics.EXTERNAL_EVENT_NOT_REGISTERED);
    }

    public void markEventMappingParsingStarted(String sourceEventId, String eventType) {
        instance.markMeter(String.format(Constants.Metrics.EVENT_MAPPING_PARSING_STARTED_PATTERN, sourceEventId, eventType));
        instance.markMeter(Constants.Metrics.EVENT_MAPPING_PARSING_STARTED);
    }

    public void markEventMappingParsingSuccess(String sourceEventId, String eventType) {
        instance.markMeter(String.format(Constants.Metrics.EVENT_MAPPING_PARSING_SUCCESS_PATTERN, sourceEventId, eventType));
        instance.markMeter(Constants.Metrics.EVENT_MAPPING_PARSING_SUCCESS);
    }

    public void markEventMappingParsingFailure(String eventType, String sourceEventId) {
        instance.markMeter(String.format(Constants.Metrics.EVENT_MAPPING_PARSING_FAILURE_PATTERN, sourceEventId, eventType));
        instance.markMeter(Constants.Metrics.EVENT_MAPPING_PARSING_FAILURE);
    }

    public void markNoInternalEvent(String name) {
        instance.markMeter(String.format(Constants.Metrics.NO_INTERNAL_EVENT_PATTERN, name));
        instance.markMeter(Constants.Metrics.NO_INTERNAL_EVENT);
    }

    public void markNotDimensionEvent(String sourceEventId, String eventType) {
        instance.markMeter(String.format(Constants.Metrics.NOT_DIMENSION_EVENT_PATTERN, sourceEventId, eventType));
        instance.markMeter(Constants.Metrics.NOT_DIMENSION_EVENT);
    }

    public void updateGroupedSize(int batchSize) {
        instance.metricRegistry.histogram("GROUP_SIZE").update(batchSize);
    }

    public void updateGroupedSize(String groupName, int batchSize) {
        instance.metricRegistry.histogram(groupName).update(batchSize);
    }

    public void histogram(String eventNameClass, long processingTime) {
        instance.metricRegistry.histogram(String.format(Constants.Metrics.STREAM_PROCESSING_TIME, eventNameClass)).update(processingTime);
    }

    public void updateBatchSize(long batchSize) {
        instance.metricRegistry.histogram("BATCH_SIZE").update(batchSize);
    }

}
