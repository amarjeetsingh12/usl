package com.flipkart.usl.processor.sample;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gap.usl.core.exception.DimensionMergeException;
import com.flipkart.gap.usl.core.exception.DimensionUpdateException;
import com.flipkart.gap.usl.core.model.dimension.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@DimensionSpecs(name = "user.payments", updateEvents = UserPaymentEvent.class, enabled = true)
@Getter
@Setter
public class UserPaymentOptionsDimension extends DimensionListBased<UserPaymentEntry, UserPaymentEvent, DummyMergeEvent> {


    @JsonCreator
    public UserPaymentOptionsDimension(@JsonProperty("entityId") String entityId) {
        super(new SizeBasedRetentionPolicy(10), entityId);
    }
    @Override
    protected void update(List<UserPaymentEvent> events) throws DimensionUpdateException {
        if (this.getElements() == null) {
            this.setElements(new ArrayList<>());
        }
        events.forEach(event -> this.getElements().add(new UserPaymentEntry(event)));
    }

    @Override
    public void merge(Dimension existingDimension, DummyMergeEvent mergeEvents) throws DimensionMergeException {
        /**
         *
         * Consume external events from kafka --> create internal events for each external event --> each dimension's internal events are groupedby entity id -->
         * List<InternalEvents> per dimension per entityId --> fetch existing instance from db given entity id and dimension
         * ExistingDimension ,List<InternalEvent>
         * call update method of ExistingDimension and pass List<InternalEvent> as parameter.
         * expiry based on ttl or size
         * persist Existing Dimension with new changes to db.
         */
    }
}
