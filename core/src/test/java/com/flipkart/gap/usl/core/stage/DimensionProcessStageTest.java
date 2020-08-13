package com.flipkart.gap.usl.core.stage;

import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import com.flipkart.gap.usl.core.model.EntityDimensionCompositeKey;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpec;
import com.flipkart.gap.usl.core.model.dimension.TestDimension;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.processor.BatchProcessor;
import com.flipkart.gap.usl.core.processor.stage.DimensionProcessStage;
import com.flipkart.gap.usl.core.processor.stage.StageProcessingException;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.flipkart.gap.usl.core.store.exception.DimensionFetchException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({})
public class DimensionProcessStageTest {
    @Mock
    private BatchProcessor batchProcessor;
    @InjectMocks
    private DimensionProcessStage batchProcessStage;

    @Before
    public void setup() {
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry("flipkart");
    }

    @Test
    public void testProcessSuccessful() throws DimensionFetchException {
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
        batchProcessStage.execute(processingStageData);
        verify(batchProcessor, times(3)).process(any(Dimension.class), anyListOf(DimensionEvent.class));
        for (DimensionMutateRequest dimensionMutateRequest : processingStageData.getDimensionMutateRequests()) {
            verify(batchProcessor, times(1)).process(dimensionMutateRequest.getDimension(), dimensionMutateRequest.getDimensionEvents());
        }
    }

    @Test(expected = StageProcessingException.class)
    public void testProcessException() throws DimensionFetchException {
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
        doThrow(new RuntimeException("Some exception")).when(batchProcessor).process(processingStageData.getDimensionMutateRequests().get(0).getDimension(),processingStageData.getDimensionMutateRequests().get(0).getDimensionEvents());
        batchProcessStage.execute(processingStageData);
    }
}
