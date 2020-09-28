package com.flipkart.gap.usl.container.responseobjects;

import lombok.Data;

import java.util.List;

@Data
public class DimensionNotFoundResponse {
    private String message;
    private static String MESSAGE_FORMAT = "Dimension %s Not found for the pivot - %s";

    public DimensionNotFoundResponse(String pivotId, String dimensionName) {
        this.message = String.format(MESSAGE_FORMAT, dimensionName, pivotId);
    }
    public DimensionNotFoundResponse(String pivotId, List<String> dimensionName) {
        this.message = String.format(MESSAGE_FORMAT, dimensionName, pivotId);
    }
}
