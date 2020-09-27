package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

@Data
public class ResilienceConfig {
    private BulkheadConfig bulkheadConfig;
    private CircuitBreakerConfig circuitBreakerConfig;
    private RateLimiterConfig rateLimiterConfig;
    private TimeLimiterConfig timeLimiterConfig;
}
