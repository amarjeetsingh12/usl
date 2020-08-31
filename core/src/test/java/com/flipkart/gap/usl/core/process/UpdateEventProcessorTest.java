package com.flipkart.gap.usl.core.process;

import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpecs;
import com.flipkart.gap.usl.core.model.dimension.TestDimension;
import com.flipkart.gap.usl.core.processor.event.MergeEventProcessor;
import com.flipkart.gap.usl.core.processor.event.UpdateEventProcessor;
import com.flipkart.gap.usl.core.store.dimension.DimensionDBRequest;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;

/**
 * Created by ankesh.maheshwari on 07/12/16.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class UpdateEventProcessorTest {

    @Mock
    private DimensionStoreDAO dimensionStoreDAO;
    @InjectMocks
    private UpdateEventProcessor updateEventProcessor;
    @InjectMocks
    private MergeEventProcessor mergeEventProcessor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry();
    }

    @Test
    public void processEventTest() throws Throwable {
        TestDimension testDimension = Mockito.mock(TestDimension.class);
        Mockito.when(testDimension.getDimensionSpecs()).thenReturn(TestDimension.class.getAnnotation(DimensionSpecs.class));
        updateEventProcessor.process(testDimension, Collections.singletonList(new TestDimension.TestDimensionUpdateEvent()));
        Mockito.verify(testDimension, Mockito.times(1)).processUpdate(anyListOf(TestDimension.TestDimensionUpdateEvent.class));
    }

    @Test
    public void mergeEventTest() throws Throwable {
        TestDimension testDimension = Mockito.mock(TestDimension.class);
        Mockito.when(testDimension.getDimensionSpecs()).thenReturn(TestDimension.class.getAnnotation(DimensionSpecs.class));
        Mockito.when(dimensionStoreDAO.getDimension(any(DimensionDBRequest.class))).thenReturn(new TestDimension("TEST"));
        mergeEventProcessor.process(testDimension, new TestDimension.TestDimensionMergeEvent());
        Mockito.verify(testDimension, Mockito.times(1)).processMerge(any(TestDimension.class), any(TestDimension.TestDimensionMergeEvent.class));
    }

}
