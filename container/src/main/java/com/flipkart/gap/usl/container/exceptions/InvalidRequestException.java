package com.flipkart.gap.usl.container.exceptions;

import org.apache.http.HttpStatus;

public class InvalidRequestException extends ServingLayerException {
    public InvalidRequestException() {
        super(HttpStatus.SC_BAD_REQUEST);
    }
    public InvalidRequestException(String message) {
        super(message, HttpStatus.SC_BAD_REQUEST);
    }
}
