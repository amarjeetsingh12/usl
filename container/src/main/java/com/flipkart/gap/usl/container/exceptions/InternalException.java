package com.flipkart.gap.usl.container.exceptions;

import org.apache.commons.httpclient.HttpStatus;

public class InternalException extends ServingLayerException {
    public InternalException() {
        super(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    public InternalException(String message) {
        super(message, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}
