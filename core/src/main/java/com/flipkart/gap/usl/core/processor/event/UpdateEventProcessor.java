package com.flipkart.gap.usl.core.processor.event;

import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpecs;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by amarjeet.singh on 15/11/16.
 */
@Singleton
@Slf4j
public class UpdateEventProcessor {

    public UpdateEventProcessor() {
    }

    public <D extends Dimension> D process(D dimensionToUpdate, List<DimensionUpdateEvent> dimensionEvents) throws Throwable {
        DimensionSpecs dimensionSpecs = dimensionToUpdate.getDimensionSpecs();
        try {
            dimensionToUpdate.processUpdate(dimensionEvents);
            JmxReporterMetricRegistry.getInstance().markDimensionUpdateSuccessMeter(String.format("%s-%s", dimensionSpecs.name(), dimensionToUpdate.getVersion()));
            return dimensionToUpdate;
        } catch (Throwable throwable) {
            log.error("Unable to send failed to update entity {},{}", dimensionToUpdate.getEntityId(), dimensionSpecs);
            JmxReporterMetricRegistry.getInstance().markDimensionUpdateFailureMeter(dimensionSpecs.name());
            log.error("Exception during dimension update entityId:{}", dimensionToUpdate.getEntityId(), throwable);
            throw throwable;
        }
    }
}
