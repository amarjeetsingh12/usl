package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

import java.io.Serializable;

@Data
public class BulkheadConfig implements Serializable {
    private int maxConcurrentCalls=100;
    private int maxWaitDuration=500;
}
