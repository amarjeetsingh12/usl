package com.flipkart.gap.usl.core.model.dimension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amarjeet.singh on 16/11/16.
 */
@DimensionSpecs(name = "test.dimension", updateEvents = TestDimension.TestDimensionUpdateEvent.class, mergeEvent = TestDimension.TestDimensionMergeEvent.class, enabled = true)
@Getter
@Setter
public class TestDimension extends DimensionListBased<TestDimension.TestDimensionElement, TestDimension.TestDimensionUpdateEvent, TestDimension.TestDimensionMergeEvent> {

    public TestDimension(String entityId, int size) {
        super(new SizeBasedRetentionPolicy(size), entityId);
    }

    @JsonCreator
    public TestDimension(@JsonProperty("entityId") String entityId) {
        super(new SizeBasedRetentionPolicy(10), entityId);
    }

    @Override
    public void merge(Dimension existingDimension, TestDimensionMergeEvent mergeEvent) {
        if (this.getEntityId().equals(mergeEvent.getTriggeringEntityId())) {
            if (this.getElements() == null) {
                this.setElements(new ArrayList<>());
            }
            final List<TestDimensionElement> finalElements = this.getElements();
            if (((TestDimension) existingDimension).getElements() != null) {
                List<TestDimensionElement> testDimensionElementList = ((TestDimension) existingDimension).getElements();
                for (TestDimensionElement testDimensionElement : testDimensionElementList) {
                    finalElements.add(testDimensionElement);
                }
            }
        } else {
            this.setElements(new ArrayList<>());
        }
    }

    @Override
    protected void update(List<TestDimension.TestDimensionUpdateEvent> events) {
        if (this.getElements() == null) {
            this.setElements(new ArrayList<>());
        }
        events.forEach(event -> this.getElements().add(new TestDimensionElement(event)));
    }

    @EventSpecs(name = "testUpdateEvent")
    @Getter
    @Setter
    public static class TestDimensionUpdateEvent extends DimensionUpdateEvent {
        private int internalField;
        private String test;
    }

    @EventSpecs(name = "testMergeEvent")
    @Getter
    @Setter
    public static class TestDimensionMergeEvent extends DimensionMergeEvent {
        private String triggeringEntityId;
    }

    @Getter
    @Setter
    public static class TestDimensionElement extends DimensionCollection.DimensionElement {
        private int internalField;

        public TestDimensionElement() {

        }

        public TestDimensionElement(TestDimensionUpdateEvent event) {
            this.setInternalField(event.getInternalField());
        }
    }
}
