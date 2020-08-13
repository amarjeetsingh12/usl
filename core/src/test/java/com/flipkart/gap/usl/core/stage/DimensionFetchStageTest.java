package com.flipkart.gap.usl.core.stage;

import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.EntityDimensionCompositeKey;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpec;
import com.flipkart.gap.usl.core.model.dimension.TestDimension;
import com.flipkart.gap.usl.core.model.dimension.TestDimension2;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.processor.stage.DimensionFetchStage;
import com.flipkart.gap.usl.core.processor.stage.StageProcessingException;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.flipkart.gap.usl.core.store.dimension.DimensionDBRequest;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.flipkart.gap.usl.core.store.exception.DimensionFetchException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({})
public class DimensionFetchStageTest {
    @Mock
    private DimensionStoreDAO dimensionStoreDAO;
    @InjectMocks
    private DimensionFetchStage dimensionFetchStage;

    @Before
    public void setup() {
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry("flipkart");
    }

    @Test
    public void testProcessSuccessful() throws DimensionFetchException {
        List<DimensionMutateRequest> dimensionMutateRequestList = new ArrayList<>();
        List<DimensionMutateRequest> responseMutateRequestList = new ArrayList<>();
        Set<DimensionDBRequest> dimensionReadDBRequests = new HashSet<>();
        Map<DimensionDBRequest, Dimension> dimensionReadResponse = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            String entityId = String.format("entity-%s", i);
            DimensionSpec dimensionSpec = new DimensionSpec(TestDimension.class, "test.dimension", 1);
            EntityDimensionCompositeKey entityDimensionCompositeKey = new EntityDimensionCompositeKey(entityId, dimensionSpec);
            List<DimensionEvent> dimensionUpdateEvents = Collections.singletonList(new TestDimension.TestDimensionUpdateEvent());
            dimensionMutateRequestList.add(new DimensionMutateRequest(entityDimensionCompositeKey, dimensionUpdateEvents));
            DimensionMutateRequest expectedDimensionMutateRequest = new DimensionMutateRequest(entityDimensionCompositeKey, dimensionUpdateEvents);
            TestDimension expectedDimension = new TestDimension(entityId);
            expectedDimensionMutateRequest.setDimension(expectedDimension);
            responseMutateRequestList.add(expectedDimensionMutateRequest);
            DimensionDBRequest dimensionReadDBRequest = new DimensionDBRequest(dimensionSpec.getDimensionClass(), entityDimensionCompositeKey.getEntityId(), dimensionSpec.getVersion());
            dimensionReadDBRequests.add(dimensionReadDBRequest);
            dimensionReadResponse.put(dimensionReadDBRequest, expectedDimension);
        }
        ProcessingStageData processingStageData = new ProcessingStageData(dimensionMutateRequestList);
        when(dimensionStoreDAO.bulkGet(dimensionReadDBRequests)).thenReturn(dimensionReadResponse);
        dimensionFetchStage.execute(processingStageData);
        Assert.assertEquals("Expected fetched dimension map not equal to processed one", responseMutateRequestList, processingStageData.getDimensionMutateRequests());
    }

    @Test
    public void testSameEntityIdMultipleDimension() throws DimensionFetchException {
        List<DimensionMutateRequest> dimensionMutateRequestList = new ArrayList<>();
        List<DimensionMutateRequest> responseMutateRequestList = new ArrayList<>();
        Set<DimensionDBRequest> dimensionReadDBRequests = new HashSet<>();
        Map<DimensionDBRequest, Dimension> dimensionReadResponse = new HashMap<>();

        String entityId = "TEST_Entity_123";
        DimensionSpec dimensionSpec = new DimensionSpec(TestDimension.class, "test.dimension", 1);
        EntityDimensionCompositeKey entityDimensionCompositeKey = new EntityDimensionCompositeKey(entityId, dimensionSpec);
        List<DimensionEvent> dimensionUpdateEvents = Collections.singletonList(new TestDimension.TestDimensionUpdateEvent());
        dimensionMutateRequestList.add(new DimensionMutateRequest(entityDimensionCompositeKey, dimensionUpdateEvents));
        DimensionMutateRequest expectedDimensionMutateRequest = new DimensionMutateRequest(entityDimensionCompositeKey, dimensionUpdateEvents);
        TestDimension expectedDimension = new TestDimension(entityId);
        expectedDimensionMutateRequest.setDimension(expectedDimension);
        responseMutateRequestList.add(expectedDimensionMutateRequest);

        DimensionDBRequest dimensionReadDBRequest = new DimensionDBRequest(dimensionSpec.getDimensionClass(), entityDimensionCompositeKey.getEntityId(), dimensionSpec.getVersion());
        dimensionReadDBRequests.add(dimensionReadDBRequest);
        dimensionReadResponse.put(dimensionReadDBRequest, expectedDimension);


        DimensionSpec dimensionSpec2 = new DimensionSpec(TestDimension2.class, "test.dimension2", 1);
        EntityDimensionCompositeKey entityDimensionCompositeKey2 = new EntityDimensionCompositeKey(entityId, dimensionSpec2);
        List<DimensionEvent> dimensionUpdateEvents2 = Collections.singletonList(new TestDimension2.TestDimensionUpdateEvent2());
        dimensionMutateRequestList.add(new DimensionMutateRequest(entityDimensionCompositeKey2, dimensionUpdateEvents2));

        DimensionMutateRequest expectedDimensionMutateRequest2 = new DimensionMutateRequest(entityDimensionCompositeKey2, dimensionUpdateEvents2);
        TestDimension2 expectedDimension2 = new TestDimension2(entityId);
        expectedDimensionMutateRequest2.setDimension(expectedDimension2);
        responseMutateRequestList.add(expectedDimensionMutateRequest2);

        DimensionDBRequest dimensionReadDBRequest2 = new DimensionDBRequest(dimensionSpec2.getDimensionClass(), entityDimensionCompositeKey2.getEntityId(), dimensionSpec2.getVersion());
        dimensionReadDBRequests.add(dimensionReadDBRequest2);
        dimensionReadResponse.put(dimensionReadDBRequest2, expectedDimension2);

        ProcessingStageData processingStageData = new ProcessingStageData(dimensionMutateRequestList);
        when(dimensionStoreDAO.bulkGet(dimensionReadDBRequests)).thenReturn(dimensionReadResponse);
        dimensionFetchStage.execute(processingStageData);
        Assert.assertEquals("Expected fetched dimension map not equal to processed one", responseMutateRequestList, processingStageData.getDimensionMutateRequests());
    }

    @Test(expected = StageProcessingException.class)
    public void testDBFailure() throws DimensionFetchException {
        List<DimensionMutateRequest> dimensionMutateRequestList = new ArrayList<>();
        Set<DimensionDBRequest> dimensionReadDBRequests = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            String entityId = String.format("entity-%s", i);
            DimensionSpec dimensionSpec = new DimensionSpec(TestDimension.class, "test.dimension", 1);
            EntityDimensionCompositeKey entityDimensionCompositeKey = new EntityDimensionCompositeKey(entityId, dimensionSpec);
            List<DimensionEvent> dimensionUpdateEvents = Collections.singletonList(new TestDimension.TestDimensionUpdateEvent());
            dimensionMutateRequestList.add(new DimensionMutateRequest(entityDimensionCompositeKey, dimensionUpdateEvents));
            DimensionDBRequest dimensionReadDBRequest = new DimensionDBRequest(dimensionSpec.getDimensionClass(), entityDimensionCompositeKey.getEntityId(), dimensionSpec.getVersion());
            dimensionReadDBRequests.add(dimensionReadDBRequest);
        }
        ProcessingStageData processingStageData = new ProcessingStageData(dimensionMutateRequestList);
        when(dimensionStoreDAO.bulkGet(dimensionReadDBRequests)).thenThrow(new RuntimeException());
        dimensionFetchStage.execute(processingStageData);
    }
}
