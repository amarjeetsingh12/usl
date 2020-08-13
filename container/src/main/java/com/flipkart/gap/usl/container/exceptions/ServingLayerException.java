package com.flipkart.gap.usl.container.exceptions;

import lombok.Getter;

abstract public class ServingLayerException extends Exception {
    @Getter
    private int httpStatus;

    ServingLayerException(int httpStatus) {
        this.httpStatus = httpStatus;
    }
    ServingLayerException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
