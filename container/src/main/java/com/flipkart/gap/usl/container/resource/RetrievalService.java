package com.flipkart.gap.usl.container.resource;

import com.flipkart.gap.usl.container.exceptions.InternalException;
import com.flipkart.gap.usl.container.exceptions.InvalidRequestException;
import com.flipkart.gap.usl.container.exceptions.ServingLayerException;
import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.registry.DimensionRegistry;
import com.flipkart.gap.usl.core.store.dimension.DimensionDBRequest;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.flipkart.gap.usl.core.store.exception.DimensionFetchException;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ashish.khatkar on 09/05/18.
 */
@Slf4j
public class RetrievalService {
    @Inject
    private DimensionStoreDAO dimensionStoreDAO;

    @Inject
    private DimensionRegistry dimensionRegistry;

    public <T extends Dimension> Optional<T> getDimensionForEntity(String dimensionName, String entityId) throws ServingLayerException {
        validateRequest(Collections.singletonList(dimensionName));

        Class<? extends Dimension> dimensionClass = dimensionRegistry.getDimensionClass(dimensionName);
        DimensionDBRequest dimensionReadDBRequest = new DimensionDBRequest(dimensionClass, entityId, Constants.DIMENSION_VERSION);
        try {
            T dimension = dimensionStoreDAO.getDimension(dimensionReadDBRequest);
            return Optional.ofNullable(dimension);
        } catch (DimensionFetchException e) {
            log.error("Exception in dimension fetch {}", dimensionReadDBRequest, e);
            throw new InternalException(e.getMessage());
        }
    }

    public Collection<Dimension> getDimensionsListForEntity(String entityId, List<String> dimensionsToFetch) throws ServingLayerException {
        validateRequest(dimensionsToFetch);

        try {
            Set<DimensionDBRequest> dimensionReadDBRequests = dimensionsToFetch.stream()
                    .map(dimension -> new DimensionDBRequest(dimensionRegistry.getDimensionClass(dimension), entityId, Constants.DIMENSION_VERSION))
                    .collect(Collectors.toSet());

            return dimensionStoreDAO.bulkGet(dimensionReadDBRequests).values();
        } catch (DimensionFetchException e) {
            log.error("Exception in bulk dimension fetch {}", dimensionsToFetch, e);
            throw new InternalException(e.getMessage());
        }
    }

    private void validateRequest(List<String> dimensions) throws ServingLayerException {
        if (dimensions.stream().anyMatch(d -> !dimensionRegistry.containsDimensionClass(d))) {
            throw new InvalidRequestException(String.format("Invalid dimensions in request.Dimensions: %s", dimensions.toString()));
        }
    }
}
