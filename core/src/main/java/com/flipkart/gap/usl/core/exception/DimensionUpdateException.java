package com.flipkart.gap.usl.core.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ayush.agarwal on 05/01/18.
 */
@Getter
@Setter
public class DimensionUpdateException extends RuntimeException {
    public DimensionUpdateException(String msg) {
        super(msg);
    }
}
