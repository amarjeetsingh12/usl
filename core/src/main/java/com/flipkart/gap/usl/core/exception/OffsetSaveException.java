package com.flipkart.gap.usl.core.exception;

public class OffsetSaveException extends RuntimeException {
    public OffsetSaveException() {
    }

    public OffsetSaveException(String message) {
        super(message);
    }

    public OffsetSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public OffsetSaveException(Throwable cause) {
        super(cause);
    }
}
