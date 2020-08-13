package com.flipkart.gap.usl.core.processor.event;

import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpecs;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEventType;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.store.dimension.DimensionDBRequest;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by amarjeet.singh on 15/11/16.
 */
@Singleton
@Slf4j
public class MergeEventProcessor {

    @Inject
    private DimensionStoreDAO dimensionStoreDAO;

    public MergeEventProcessor() {
    }

    public Dimension process(Dimension toDimension, DimensionMergeEvent dimensionMergeEvent) throws Throwable {
        Class<? extends Dimension> dimensionClass = toDimension.getClass();
        DimensionSpecs dimensionSpecs = toDimension.getDimensionSpecs();
        try {
            if (dimensionMergeEvent.getEventType().equals(DimensionEventType.MERGE)) {
                try {
                    DimensionDBRequest dimensionReadDBRequest = new DimensionDBRequest(dimensionClass, dimensionMergeEvent.getFromEntityId(), toDimension.getVersion());
                    Dimension fromDimension = dimensionStoreDAO.getDimension(dimensionReadDBRequest);
                    if (fromDimension == null) {
                        fromDimension = dimensionClass.getConstructor(String.class).newInstance(dimensionMergeEvent.getFromEntityId());
                    }
                    // Create deep copy of fromDimension to avoid changes due to merge call
                    Dimension fromDimensionDeepCopy = ObjectMapperFactory.deepCopy(fromDimension);
                    try {
                        fromDimension.processMerge(toDimension, dimensionMergeEvent);
                        JmxReporterMetricRegistry.getInstance().markDimensionMergeSuccessMeter(String.format("%s-%s", dimensionSpecs.name(), toDimension.getVersion()));
                    } catch (Throwable throwable) {
                        JmxReporterMetricRegistry.getInstance().markDimensionMergeFailureMeter(String.format("%s-%s", dimensionSpecs.name(), toDimension.getVersion()));
                        log.error("Error merging dimension to:{},from:{}", toDimension, fromDimension);
                        throw throwable;
                    }
                    try {
                        toDimension.processMerge(fromDimensionDeepCopy, dimensionMergeEvent);
                        JmxReporterMetricRegistry.getInstance().markDimensionMergeSuccessMeter(String.format("%s-%s", dimensionSpecs.name(), toDimension.getVersion()));
                    } catch (Throwable throwable) {
                        JmxReporterMetricRegistry.getInstance().markDimensionMergeFailureMeter(String.format("%s-%s", dimensionSpecs.name(), toDimension.getVersion()));
                        log.error("Error merging dimension to:{},from:{}", toDimension, fromDimension);
                        throw throwable;
                    }
                } catch (Throwable throwable) {
                    JmxReporterMetricRegistry.getMetricRegistry().meter(name(MergeEventProcessor.class, "ExceptionInDimensionMerge")).mark();
                    log.error("Exception during dimension merge ", throwable);
                    throw throwable;
                }
            } else {
                JmxReporterMetricRegistry.getMetricRegistry().meter(name(MergeEventProcessor.class, "ExceptionInDimensionMerge")).mark();
                log.error("Got merge request from non merge Event {}", dimensionMergeEvent.getClass());
            }
        } catch (Throwable throwable) {
            try {
            } catch (Exception e) {
                log.error("Unable to send failed to merge event {},{},{}", toDimension.getEntityId(), dimensionSpecs.name(), toDimension.getVersion());
                throw e;
            }
            JmxReporterMetricRegistry.getMetricRegistry().meter(name(MergeEventProcessor.class, "ExceptionInDimensionMerge")).mark();
            log.error("Exception during dimension merge ", throwable);
            throw throwable;
        }
        return toDimension;
    }
}
