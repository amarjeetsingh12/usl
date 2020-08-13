package com.flipkart.gap.usl.core.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by amarjeet.singh on 04/10/16.
 */
@Getter
@Setter
public class DimensionRegistryException extends Exception {

    private String dimensionName;
    private String reason;

    public DimensionRegistryException(String dimensionName, String reason) {
        this.dimensionName = dimensionName;
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        return String.format("Exception for dimension:%s.Reason %s", dimensionName, reason);
    }
}
