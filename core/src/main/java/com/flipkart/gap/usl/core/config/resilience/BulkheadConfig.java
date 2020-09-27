package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

@Data
public class BulkheadConfig {
    private int maxConcurrentCalls=100;
    private int maxWaitDuration=500;
}
