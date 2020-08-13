package com.flipkart.gap.usl.core.process;

import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.TestDimension;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import com.flipkart.gap.usl.core.processor.BatchProcessor;
import com.flipkart.gap.usl.core.processor.event.MergeEventProcessor;
import com.flipkart.gap.usl.core.processor.event.UpdateEventProcessor;
import com.flipkart.gap.usl.core.registry.DimensionRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by amarjeet.singh on 17/11/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DimensionRegistry.class})
@PowerMockIgnore("javax.management.*")
public class BatchProcessorTest {
    @Mock
    private UpdateEventProcessor updateEventProcessor;
    @Mock
    private MergeEventProcessor mergeEventProcessor;
    @Mock
    private DimensionRegistry dimensionRegistry;
    @InjectMocks
    private BatchProcessor batchProcessor;

    @Test
    public void testSingleMergeEvent() throws Throwable {
        reset(mergeEventProcessor);
        reset(updateEventProcessor);
        String entityId = "TEST-123";
        List<DimensionEvent> dimensionEvents = new ArrayList<>();
        dimensionEvents.add(new TestDimension.TestDimensionMergeEvent());
        batchProcessor.process(new TestDimension(entityId), dimensionEvents);
        verify(mergeEventProcessor, times(1)).process(any(Dimension.class), any(DimensionMergeEvent.class));
        verify(updateEventProcessor, times(0)).process(any(Dimension.class), anyListOf(DimensionUpdateEvent.class));
    }

    @Test
    public void testMultipleMergeEvents() throws Throwable {
        reset(mergeEventProcessor);
        reset(updateEventProcessor);
        List<DimensionEvent> dimensionEvents = new ArrayList<>();
        dimensionEvents.add(new TestDimension.TestDimensionMergeEvent());
        dimensionEvents.add(new TestDimension.TestDimensionMergeEvent());
        batchProcessor.process(new TestDimension("TEST-123"),dimensionEvents);
        verify(mergeEventProcessor, times(2)).process(any(Dimension.class), any(DimensionMergeEvent.class));
        verify(updateEventProcessor, times(0)).process(any(Dimension.class), anyListOf(DimensionUpdateEvent.class));
    }

    @Test
    public void testSingleUpdateEvent() throws Throwable {
        reset(mergeEventProcessor);
        reset(updateEventProcessor);
        List<DimensionEvent> dimensionEvents = new ArrayList<>();
        dimensionEvents.add(new TestDimension.TestDimensionUpdateEvent());
        batchProcessor.process(new TestDimension("TEST-123"),dimensionEvents);
        verify(mergeEventProcessor, times(0)).process(any(Dimension.class), any(DimensionMergeEvent.class));
        verify(updateEventProcessor, times(1)).process(any(Dimension.class), anyListOf(DimensionUpdateEvent.class));
    }


    @Test
    public void testMultipleUpdateEvents() throws Throwable {
        reset(mergeEventProcessor);
        reset(updateEventProcessor);
        List<DimensionEvent> dimensionEvents = new ArrayList<>();
        dimensionEvents.add(new TestDimension.TestDimensionUpdateEvent());
        dimensionEvents.add(new TestDimension.TestDimensionUpdateEvent());
        batchProcessor.process(new TestDimension("TEST-123"),dimensionEvents);
        verify(mergeEventProcessor, times(0)).process(any(Dimension.class), any(DimensionMergeEvent.class));
        verify(updateEventProcessor, times(1)).process(any(Dimension.class), anyListOf(DimensionUpdateEvent.class));
    }

    @Test
    public void testMultipleMixEvents() throws Throwable {
        reset(mergeEventProcessor);
        reset(updateEventProcessor);
        List<DimensionEvent> dimensionEvents = new ArrayList<>();
        dimensionEvents.add(new TestDimension.TestDimensionUpdateEvent());
        dimensionEvents.add(new TestDimension.TestDimensionMergeEvent());
        batchProcessor.process(new TestDimension("TEST-123"),dimensionEvents);
        verify(mergeEventProcessor, times(1)).process(any(Dimension.class), any(DimensionMergeEvent.class));
        verify(updateEventProcessor, times(1)).process(any(Dimension.class), anyListOf(DimensionUpdateEvent.class));
    }

    @Test
    public void testMultipleMixAcrossProcessEntityEvents() throws Throwable {
        reset(mergeEventProcessor);
        reset(updateEventProcessor);
        for (int i = 0; i < 10; i++) {
            List<DimensionEvent> dimensionEvents = new ArrayList<>();
            dimensionEvents.add(new TestDimension.TestDimensionUpdateEvent());
            dimensionEvents.add(new TestDimension.TestDimensionMergeEvent());
            batchProcessor.process(new TestDimension("TEST-" + i),dimensionEvents);
        }
        verify(mergeEventProcessor, times(10)).process(any(Dimension.class), any(DimensionMergeEvent.class));
        verify(updateEventProcessor, times(10)).process(any(Dimension.class), anyListOf(DimensionUpdateEvent.class));
    }

}
