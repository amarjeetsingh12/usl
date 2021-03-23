package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResilienceConfig implements Serializable {
    private BulkheadConfig bulkheadConfig;
    private CircuitBreakerConfig circuitBreakerConfig;
    private RateLimiterConfig rateLimiterConfig;
    private TimeLimiterConfig timeLimiterConfig;
}
