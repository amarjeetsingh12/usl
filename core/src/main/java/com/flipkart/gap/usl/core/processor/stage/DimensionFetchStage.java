package com.flipkart.gap.usl.core.processor.stage;

import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpec;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.flipkart.gap.usl.core.store.dimension.DimensionDBRequest;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Singleton
@Slf4j
public class DimensionFetchStage extends ProcessingStage {
    @Inject
    private DimensionStoreDAO dimensionStoreDAO;

    @Override
    protected void process(ProcessingStageData processingStageData) throws StageProcessingException {
        Set<DimensionDBRequest> dimensionReadDBRequests = new HashSet<>();
        Map<DimensionDBRequest, DimensionMutateRequest> flattenedMap = new HashMap<>();
        processingStageData.getDimensionMutateRequests().forEach(dimensionMutateRequest -> {
            String entityId = dimensionMutateRequest.getEntityDimensionCompositeKey().getEntityId();
            DimensionSpec dimensionSpec = dimensionMutateRequest.getEntityDimensionCompositeKey().getDimensionSpec();
            DimensionDBRequest dimensionReadDBRequest = new DimensionDBRequest(dimensionSpec.getDimensionClass(), entityId, dimensionSpec.getVersion());
            dimensionReadDBRequests.add(dimensionReadDBRequest);
            flattenedMap.put(dimensionReadDBRequest, dimensionMutateRequest);

        });
        try {
            Map<DimensionDBRequest, Dimension> dimensionsFetched = dimensionStoreDAO.bulkGet(dimensionReadDBRequests);
            dimensionsFetched.forEach((dimensionDBReadRequest, dimension) -> {
                if (flattenedMap.containsKey(dimensionDBReadRequest)) {
                    flattenedMap.get(dimensionDBReadRequest).setDimension(dimension);
                } else {
                    throw new StageProcessingException(String.format("Dimension fetched does not exist in request map requested %s, fetched %s", flattenedMap.keySet(), dimensionDBReadRequest));
                }
            });
            flattenedMap.entrySet().stream().filter(processEntity -> processEntity.getValue().getDimension() == null).forEach(entry->{
                DimensionDBRequest dimensionReadDBRequest = entry.getKey();
                try {
                    entry.getValue().setDimension(dimensionReadDBRequest.getDimensionClass().getConstructor(String.class).newInstance(dimensionReadDBRequest.getEntityId()));
                } catch (InstantiationException|IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
                    throw new StageProcessingException(e);
                }
            });
            if (flattenedMap.values().stream().anyMatch(processEntity -> processEntity.getDimension() == null)) {
                log.error("Dimensions requested and fetched are not equal {}!={}", flattenedMap, dimensionsFetched);
                throw new StageProcessingException(String.format("Dimensions requested and fetched are not equal %s!=%s", flattenedMap, dimensionsFetched));
            }
        } catch (Throwable e) {
            throw new StageProcessingException(e);
        }
    }
}
