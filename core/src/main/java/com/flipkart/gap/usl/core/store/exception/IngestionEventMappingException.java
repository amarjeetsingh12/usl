package com.flipkart.gap.usl.core.store.exception;

/**
 * Created by amarjeet.singh on 05/10/16.
 */
public class IngestionEventMappingException extends Exception {
    public IngestionEventMappingException(String message) {
        super(message);
    }

    public IngestionEventMappingException(Throwable cause) {
        super(cause);
    }
}
