package com.flipkart.gap.usl.core.store.dimension.resilence;

public class DecoratorExecutionException extends Exception{
    public DecoratorExecutionException() {
        super();
    }

    public DecoratorExecutionException(String message) {
        super(message);
    }

    public DecoratorExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecoratorExecutionException(Throwable cause) {
        super(cause);
    }
}
