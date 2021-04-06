package com.flipkart.gap.usl.container.entry;

import lombok.Data;

import java.util.List;

@Data
public class Request {
    private List<String> entityIds;
    private List<String> dimensionNames;
}
