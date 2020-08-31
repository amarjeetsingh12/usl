package com.flipkart.gap.usl.core.stage;

import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.EntityDimensionCompositeKey;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpec;
import com.flipkart.gap.usl.core.model.dimension.TestDimension;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.processor.stage.DimensionSaveStage;
import com.flipkart.gap.usl.core.processor.stage.StageProcessingException;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.flipkart.gap.usl.core.store.exception.DimensionPersistException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({})
public class DimensionSaveStageTest {
    @Mock
    private DimensionStoreDAO dimensionStoreDAO;
    @InjectMocks
    private DimensionSaveStage dimensionSaveStage;

    @Before
    public void setup() {
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry();
    }

    @Test
    public void testProcessSuccessful() throws DimensionPersistException {
        List<DimensionMutateRequest> dimensionMutateRequestList = new ArrayList<>();
        Set<Dimension> dimensionToSave = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            String entityId = String.format("entity-%s", i);
            DimensionSpec dimensionSpec = new DimensionSpec(TestDimension.class, "test.dimension", 1);
            EntityDimensionCompositeKey entityDimensionCompositeKey = new EntityDimensionCompositeKey(entityId, dimensionSpec);
            List<DimensionEvent> dimensionUpdateEvents = Collections.singletonList(new TestDimension.TestDimensionUpdateEvent());
            DimensionMutateRequest dimensionMutateRequest = new DimensionMutateRequest(entityDimensionCompositeKey, dimensionUpdateEvents);
            dimensionMutateRequest.setDimension(new TestDimension(entityId));
            dimensionToSave.add(dimensionMutateRequest.getDimension());
            dimensionMutateRequestList.add(dimensionMutateRequest);
        }
        ProcessingStageData processingStageData = new ProcessingStageData(dimensionMutateRequestList);
        dimensionSaveStage.execute(processingStageData);
        verify(dimensionStoreDAO, times(1)).bulkSave(dimensionToSave);
    }

    @Test(expected = StageProcessingException.class)
    public void testProcessException() throws DimensionPersistException {
        List<DimensionMutateRequest> dimensionMutateRequestList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String entityId = String.format("entity-%s", i);
            DimensionSpec dimensionSpec = new DimensionSpec(TestDimension.class, "test.dimension", 1);
            EntityDimensionCompositeKey entityDimensionCompositeKey = new EntityDimensionCompositeKey(entityId, dimensionSpec);
            List<DimensionEvent> dimensionUpdateEvents = Collections.singletonList(new TestDimension.TestDimensionUpdateEvent());
            DimensionMutateRequest dimensionMutateRequest = new DimensionMutateRequest(entityDimensionCompositeKey, dimensionUpdateEvents);
            dimensionMutateRequest.setDimension(new TestDimension(entityId));
            dimensionMutateRequestList.add(dimensionMutateRequest);
        }
        ProcessingStageData processingStageData = new ProcessingStageData(dimensionMutateRequestList);
        doThrow(new DimensionPersistException("Some exception")).when(dimensionStoreDAO).bulkSave(anySetOf(Dimension.class));
        dimensionSaveStage.execute(processingStageData);
    }
}
