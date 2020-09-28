package com.flipkart.gap.usl.container.responseobjects;

import lombok.Data;

@Data
public class EventIngestionSuccess {
    private String message;

    public EventIngestionSuccess(String message) {
        this.message = message;
    }
}
