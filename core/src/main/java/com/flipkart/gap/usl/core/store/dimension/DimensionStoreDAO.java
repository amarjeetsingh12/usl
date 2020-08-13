package com.flipkart.gap.usl.core.store.dimension;

import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.store.exception.DimensionDeleteException;
import com.flipkart.gap.usl.core.store.exception.DimensionFetchException;
import com.flipkart.gap.usl.core.store.exception.DimensionPersistException;
import com.flipkart.gap.usl.core.store.exception.DimensionPersistRuntimeException;

import java.util.Map;
import java.util.Set;

/**
 * Created by ankesh.maheshwari on 20/10/16.
 */
public interface DimensionStoreDAO {

    public <T extends Dimension> T getDimension(DimensionDBRequest dimensionReadDBRequest) throws DimensionFetchException;

    public <T extends Dimension> T getDimension(DimensionDBRequest dimensionReadDBRequest, String poolName) throws DimensionFetchException;

    public <T extends Dimension> T getDimension(DimensionDBRequest dimensionReadDBRequest, String poolName, boolean useCache) throws DimensionFetchException;

    public Map<DimensionDBRequest, Dimension> bulkGet(Set<DimensionDBRequest> dimensionReadDBRequests) throws DimensionFetchException;

    public void bulkSave(Set<Dimension> dimensions) throws DimensionPersistRuntimeException, DimensionPersistException;

    void deleteDimension(DimensionDBRequest dimensionReadDBRequest, String poolName) throws DimensionDeleteException;
}
