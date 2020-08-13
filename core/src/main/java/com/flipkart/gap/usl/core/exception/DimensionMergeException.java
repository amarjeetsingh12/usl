package com.flipkart.gap.usl.core.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ayush.agarwal on 05/01/18.
 */
@Getter
@Setter
public class DimensionMergeException extends RuntimeException {
    public DimensionMergeException(String msg) {
        super(msg);
    }
}
