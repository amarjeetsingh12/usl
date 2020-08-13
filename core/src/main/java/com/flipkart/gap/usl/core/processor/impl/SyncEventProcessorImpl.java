package com.flipkart.gap.usl.core.processor.impl;

import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.EntityDimensionCompositeKey;
import com.flipkart.gap.usl.core.model.InternalEventMeta;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.model.external.ExternalEventSchema;
import com.flipkart.gap.usl.core.processor.ExternalEventHelper;
import com.flipkart.gap.usl.core.processor.SyncEventProcessor;
import com.flipkart.gap.usl.core.processor.exception.ProcessingException;
import com.flipkart.gap.usl.core.processor.stage.DimensionFetchStage;
import com.flipkart.gap.usl.core.processor.stage.DimensionProcessStage;
import com.flipkart.gap.usl.core.processor.stage.DimensionSaveStage;
import com.flipkart.gap.usl.core.processor.stage.StageProcessingException;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import scala.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ashish.khatkar on 16/10/18.
 */
@Singleton
public class SyncEventProcessorImpl implements SyncEventProcessor {

    @Inject
    private DimensionFetchStage dimensionFetchStage;

    @Inject
    private DimensionProcessStage dimensionProcessStage;

    @Inject
    private DimensionSaveStage dimensionSaveStage;

    @Inject
    private ExternalEventHelper externalEventHelper;

    @Override
    public ProcessingStageData processEvent(ExternalEventSchema externalEvent) throws ProcessingException {
        try {
            List<Tuple2<EntityDimensionCompositeKey, InternalEventMeta>> internalEventsMeta = externalEventHelper.findInternalEventsForExternalEvent(externalEvent);
            Map<EntityDimensionCompositeKey, List<Tuple2<EntityDimensionCompositeKey, InternalEventMeta>>> groupedMap = internalEventsMeta.stream().collect(Collectors.groupingBy(tuple -> tuple._1));

            List<DimensionMutateRequest> dimensionUpdateEvents = groupedMap.entrySet().stream()
                    .flatMap(this::getDimensionMutateRequestStreamFromGroupedMapEntry)
                    .collect(Collectors.toList());

            ProcessingStageData processingStageData = new ProcessingStageData(dimensionUpdateEvents);

            dimensionFetchStage.execute(processingStageData);
            dimensionProcessStage.execute(processingStageData);
            dimensionSaveStage.execute(processingStageData);

            return processingStageData;
        } catch (StageProcessingException | ExecutionException throwable) {
            throw new ProcessingException(throwable);
        }
    }

    private Stream<DimensionMutateRequest> getDimensionMutateRequestStreamFromGroupedMapEntry(Map.Entry<EntityDimensionCompositeKey, List<Tuple2<EntityDimensionCompositeKey, InternalEventMeta>>> entry) {
        List<InternalEventMeta> internalEventMetas = entry.getValue().stream().map(tuple2 -> tuple2._2).collect(Collectors.toList());

        List<DimensionEvent> dimensionEvents = internalEventMetas.stream()
                .map(InternalEventMeta::getDimensionEvent)
                .collect(Collectors.toList());

        if (dimensionEvents != null && !dimensionEvents.isEmpty() && !internalEventMetas.isEmpty()) {
            JmxReporterMetricRegistry.getInstance().updateGroupedSize(dimensionEvents.size());
            return Stream.of(new DimensionMutateRequest(entry.getKey(), dimensionEvents));
        } else {
            return null;
        }
    }
}
