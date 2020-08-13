package com.flipkart.gap.usl.core.store.exception;

import lombok.Getter;

@Getter
public class DimensionDeleteException extends Exception {

    private int code = 500;

    public DimensionDeleteException(String message) {
        super(message);
    }

    public DimensionDeleteException(int code, String message) {
        super(message);
        this.code = code;
    }

    public DimensionDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public DimensionDeleteException(Throwable cause) {
        super(cause);
    }

}
