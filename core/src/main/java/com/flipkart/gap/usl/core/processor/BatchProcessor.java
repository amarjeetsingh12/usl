package com.flipkart.gap.usl.core.processor;

import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import com.flipkart.gap.usl.core.processor.event.MergeEventProcessor;
import com.flipkart.gap.usl.core.processor.event.UpdateEventProcessor;
import com.flipkart.gap.usl.core.processor.stage.StageProcessingException;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by amarjeet.singh on 15/11/16.
 */
@Slf4j
public class BatchProcessor {
    @Inject
    private UpdateEventProcessor updateEventProcessor;
    @Inject
    private MergeEventProcessor mergeEventProcessor;

    public void process(Dimension dimension, List<DimensionEvent> dimensionEvents) {
        dimensionEvents.sort(Comparator.comparingLong(DimensionEvent::getCreated));
        if (dimensionEvents.size() > 0) {
            DimensionEvent currentEvent = dimensionEvents.get(0);
            Class<? extends DimensionEvent> lastEventClass = currentEvent.getClass();
            List<List<DimensionEvent>> groupedEvents = new ArrayList<>();
            List<DimensionEvent> continuousSimilarEvents = new ArrayList<>();
            continuousSimilarEvents.add(currentEvent);
            for (int i = 1; i < dimensionEvents.size(); i++) {
                currentEvent = dimensionEvents.get(i);
                Class<? extends DimensionEvent> currentEventClass = currentEvent.getClass();
                if (lastEventClass.equals(currentEventClass)) {
                    continuousSimilarEvents.add(currentEvent);
                } else {
                    groupedEvents.add(continuousSimilarEvents);
                    continuousSimilarEvents = new ArrayList<>();
                    continuousSimilarEvents.add(currentEvent);
                    lastEventClass = currentEventClass;
                }
            }

            if (!continuousSimilarEvents.isEmpty()) {
                groupedEvents.add(continuousSimilarEvents);
            } else {
                log.error("No events found in this batch!");
            }
            groupedEvents.forEach(currentEvents -> {
                try {
                    processEvent(currentEvents, dimension);
                } catch (Throwable throwable) {
                    throw new StageProcessingException(throwable);
                }
            });
        }

    }

    private void processEvent(List<? extends DimensionEvent> dimensionEvents, Dimension dimension) throws Throwable {
        try {
            DimensionEvent dimensionEvent = dimensionEvents.get(0);
            switch (dimensionEvent.getEventType()) {
                case MERGE:
                    for (DimensionEvent currentDimensionEvent : dimensionEvents) {
                        mergeEventProcessor.process(dimension, (DimensionMergeEvent) currentDimensionEvent);
                    }
                    break;
                case UPDATE:
                    updateEventProcessor.process(dimension, (List<DimensionUpdateEvent>) dimensionEvents);
                    break;
                default:
                    log.error("unknown type of dimension Event {}", dimensionEvent);
            }
        } catch (Throwable t) {
            JmxReporterMetricRegistry.getMetricRegistry().meter(name(BatchProcessor.class, "ErrorProcessingEvents")).mark();
            log.error("Error processing Events {} for Dimension{},{}", dimensionEvents, dimension, t);
            throw t;
        }
    }
}
