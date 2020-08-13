package com.flipkart.gap.usl.core.model.dimension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.exception.DimensionUpdateException;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@DimensionSpecs(name = "test.dimension2", updateEvents = TestDimension2.TestDimensionUpdateEvent2.class, mergeEvent = TestDimension2.TestDimensionMergeEvent2.class, enabled = true)
@Getter
@Setter
public class TestDimension2 extends DimensionListBased<TestDimension2.TestDimensionElement2, TestDimension2.TestDimensionUpdateEvent2, TestDimension2.TestDimensionMergeEvent2> {


    public TestDimension2(String entityId, int size) {
        super(new SizeBasedRetentionPolicy(size), entityId);
    }

    @JsonCreator
    public TestDimension2(@JsonProperty("entityId") String entityId) {
        super(new SizeBasedRetentionPolicy(10), entityId);
    }

    @Override
    protected void update(List<TestDimensionUpdateEvent2> events) throws DimensionUpdateException {
        if (this.getElements() == null) {
            this.setElements(new ArrayList<>());
        }
        List<TestDimensionElement2> testDimensionElements = new ArrayList<TestDimensionElement2>();
        for (TestDimensionUpdateEvent2 event : events) {
            testDimensionElements.add(new TestDimensionElement2(event));
        }
        this.setElements(testDimensionElements);
    }

    @Override
    public void merge(Dimension existingDimension, TestDimensionMergeEvent2 mergeEvent) {
        if (this.getEntityId().equals(mergeEvent.getTriggeringEntityId())) {
            if (this.getElements() == null) {
                this.setElements(new ArrayList<>());
            }
            final List<TestDimensionElement2> finalElements = this.getElements();
            if (((TestDimension2) existingDimension).getElements() != null) {
                List<TestDimensionElement2> testDimensionElementList = ((TestDimension2) existingDimension).getElements();
                for (TestDimensionElement2 testDimensionElement : testDimensionElementList) {
                    finalElements.add(testDimensionElement);
                }
            }
        } else {
            this.setElements(new ArrayList<>());
        }
    }

    @EventSpecs(name = "testUpdateEvent2")
    @Getter
    @Setter
    public static class TestDimensionUpdateEvent2 extends DimensionUpdateEvent {
        private int internalField;
        private String test;
    }

    @EventSpecs(name = "testMergeEvent2")
    @Getter
    @Setter
    public static class TestDimensionMergeEvent2 extends DimensionMergeEvent {
        private String triggeringEntityId;
    }

    @Getter
    @Setter
    public static class TestDimensionElement2 extends DimensionCollection.DimensionElement {
        private int internalField;

        public TestDimensionElement2() {

        }

        public TestDimensionElement2(TestDimensionUpdateEvent2 event) {
            this.setInternalField(event.getInternalField());
        }
    }
}
