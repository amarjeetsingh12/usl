package com.flipkart.usl.processor.sample;

import com.flipkart.gap.usl.core.model.dimension.DimensionCollection;
import com.flipkart.gap.usl.core.model.dimension.EventSpecs;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPaymentEntry extends DimensionCollection.DimensionElement {
    private String uidx;
    private String paymentOption;
    private String provider;
    private String status;
    private String bankName;

    public UserPaymentEntry(UserPaymentEvent userPaymentEvent) {
        this.uidx = userPaymentEvent.getUidx();
        this.paymentOption = userPaymentEvent.getPaymentOption();
        this.provider = userPaymentEvent.getProvider();
        this.status = userPaymentEvent.getStatus();
        this.bankName = userPaymentEvent.getBankName();
        this.setCreated(userPaymentEvent.getCreated());
        this.setUpdated(userPaymentEvent.getCreated());
    }
}
