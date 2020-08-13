package com.flipkart.gap.usl.core.store.exception;

public class DimensionFetchException extends Exception {
    public DimensionFetchException(String message) {
        super(message);
    }

    public DimensionFetchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DimensionFetchException(Throwable cause) {
        super(cause);
    }
}
