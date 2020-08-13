package com.flipkart.gap.usl.container.resource;

import com.flipkart.gap.usl.container.exceptions.ServingLayerException;
import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.registry.DimensionRegistry;
import com.flipkart.gap.usl.core.store.dimension.DimensionDBRequest;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.flipkart.gap.usl.core.store.exception.DimensionDeleteException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DimensionDeletionService {

    RetrievalService retrievalService;
    DimensionStoreDAO dimensionStoreDAO;
    DimensionRegistry dimensionRegistry;

    static String HBASE_POOL_NAME = "<your-pool-name>";

    public void deleteEntity(String entityId, String dimensionName) throws DimensionDeleteException {
        try {
            Optional<Dimension> dimension = retrievalService.getDimensionForEntity(dimensionName, entityId);
            if (dimension.isPresent()) {
                Class<? extends Dimension> dimensionClass = dimensionRegistry.getDimensionClass(dimensionName);
                DimensionDBRequest dimensionDBRequest = new DimensionDBRequest(dimensionClass, entityId, Constants.DIMENSION_VERSION);
                dimensionStoreDAO.deleteDimension(dimensionDBRequest, HBASE_POOL_NAME);
            } else {
                throw new DimensionDeleteException(404, "Dimension not found");
            }
        } catch (ServingLayerException e) {
            throw new DimensionDeleteException(e.getMessage(), e);
        }
    }

}
