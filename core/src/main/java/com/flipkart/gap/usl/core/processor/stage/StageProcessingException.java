package com.flipkart.gap.usl.core.processor.stage;

public class StageProcessingException extends RuntimeException {
    public StageProcessingException(Throwable cause) {
        super(cause);
    }

    public StageProcessingException(String message) {
        super(message);
    }
}
