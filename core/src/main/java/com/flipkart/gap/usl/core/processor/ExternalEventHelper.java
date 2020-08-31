package com.flipkart.gap.usl.core.processor;

import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.EntityDimensionCompositeKey;
import com.flipkart.gap.usl.core.model.InternalEventMeta;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.model.event.ExternalEvent;
import com.flipkart.gap.usl.core.model.external.ExternalEventSchema;
import com.flipkart.gap.usl.core.registry.DimensionRegistry;
import com.flipkart.gap.usl.core.store.event.EventTypeDBWrapper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class ExternalEventHelper {
    @Inject
    private EventTypeDBWrapper eventTypeDBWrapper;
    @Inject
    private DimensionRegistry dimensionRegistry;

    public Iterator<Tuple2<EntityDimensionCompositeKey, InternalEventMeta>> findInternalEventsForExternalEvent(byte[] record) throws IOException, ExecutionException {
        JmxReporterMetricRegistry.getInstance().markMeter(Constants.Metrics.EXTERNAL_EVENT);
        ExternalEventSchema externalEvent = ObjectMapperFactory.getMapper().readValue(record, ExternalEventSchema.class);
        if (StringUtils.isBlank(externalEvent.getName())) {
            log.error("EventId missing from external Event");
            JmxReporterMetricRegistry.getInstance().markMeter(Constants.Metrics.EXTERNAL_EVENT_MISSING_NAME);
            return Collections.emptyIterator();
        }
        return findInternalEventsForExternalEvent(externalEvent).iterator();
    }

    public List<Tuple2<EntityDimensionCompositeKey, InternalEventMeta>> findInternalEventsForExternalEvent(ExternalEventSchema externalEvent) throws ExecutionException {
        JmxReporterMetricRegistry.getInstance().markMeter(externalEvent.getName());
        ExternalEvent externalEventDefinition;
        externalEventDefinition = eventTypeDBWrapper.getExternalEvent(externalEvent.getName());
        if (externalEventDefinition != null) {
            return dimensionRegistry.getDimensionEvents(externalEvent).stream()
                    .flatMap(dimensionEvent -> dimensionRegistry.convertEventToInternalEventMeta(dimensionEvent))
                    .collect(Collectors.toList());
        } else {
            JmxReporterMetricRegistry.getInstance().markExternalEventNotRegistered(externalEvent.getName());
            log.info("External Event not registered with name {}", externalEvent.getName());
            return Collections.emptyList();
        }
    }

    List<List<DimensionMutateRequest>> createSubListPartitions(Iterator<Tuple2<EntityDimensionCompositeKey, Iterable<InternalEventMeta>>> groupedPartitionIterator, int batchSize) {
        return createSubListPartitions(Lists.newArrayList(groupedPartitionIterator), batchSize);
    }

    private List<List<DimensionMutateRequest>> createSubListPartitions(List<Tuple2<EntityDimensionCompositeKey, Iterable<InternalEventMeta>>> groupedInternalEvents, int batchSize) {
        List<DimensionMutateRequest> dimensionUpdateEvents = groupedInternalEvents.stream().flatMap(tuple -> {
            List<InternalEventMeta> internalEventMetas = Lists.newArrayList(tuple._2);
            List<DimensionEvent> dimensionEvents = internalEventMetas.stream()
                    .map(InternalEventMeta::getDimensionEvent)
                    .collect(Collectors.toList());
            if (!dimensionEvents.isEmpty() && !internalEventMetas.isEmpty()) {
                int groupedSize = dimensionEvents.size();
                if (groupedSize > 50) {
                    log.info("Found grouped dimension events of size {} which is greater than 50!, group key is {}", groupedSize, tuple._1);
                }
                JmxReporterMetricRegistry.getInstance().updateGroupedSize(groupedSize);
                return Stream.of(new DimensionMutateRequest(tuple._1, dimensionEvents));
            } else {
                return null;
            }
        }).collect(Collectors.toList());
        //This is because Lists.partition returns ArrayList.SubList which is not serializable.
        return Lists.partition(dimensionUpdateEvents, batchSize).stream().map(ArrayList::new).collect(Collectors.toList());
    }
}
