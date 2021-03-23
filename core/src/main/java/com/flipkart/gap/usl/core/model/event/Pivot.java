package com.flipkart.gap.usl.core.model.event;

import lombok.Data;

import java.util.List;

@Data
public class Pivot {
    private String type;
    private List<String> value;
    private String expression;
}
