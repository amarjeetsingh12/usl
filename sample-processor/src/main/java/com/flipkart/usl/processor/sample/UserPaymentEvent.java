package com.flipkart.usl.processor.sample;

import com.flipkart.gap.usl.core.model.dimension.EventSpecs;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import lombok.Data;

@EventSpecs(name = "userPaymentEvent")
@Data
public class UserPaymentEvent extends DimensionUpdateEvent {
    private String uidx;
    private String paymentOption;
    private String provider;
    private String status;
    private String bankName;
}
