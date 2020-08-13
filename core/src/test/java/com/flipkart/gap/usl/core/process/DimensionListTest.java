package com.flipkart.gap.usl.core.process;

import com.flipkart.gap.usl.core.model.dimension.TestDimension;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankesh.maheshwari on 07/12/16.
 */
public class DimensionListTest {
    @Test
    public void DimensionListSizeTest() throws Throwable {
        TestDimension testDimension = new TestDimension("T1",1);
        TestDimension testDimension1 = new TestDimension("T2", 10);
        TestDimension testDimension2 = new TestDimension("T3", 12);
        List<TestDimension.TestDimensionUpdateEvent> viewedUpdateEvents = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            TestDimension.TestDimensionUpdateEvent updateEvent = new TestDimension.TestDimensionUpdateEvent();
            updateEvent.setCreated(System.currentTimeMillis());
            updateEvent.setInternalField(112);
            viewedUpdateEvents.add(updateEvent);
        }
        testDimension.processUpdate(viewedUpdateEvents);
        testDimension1.processUpdate(viewedUpdateEvents);
        testDimension2.processUpdate(viewedUpdateEvents);
        Assert.assertEquals(1, testDimension.getElements().size());
        Assert.assertEquals(10, testDimension1.getElements().size());
        Assert.assertEquals(12, testDimension2.getElements().size());
    }
}
