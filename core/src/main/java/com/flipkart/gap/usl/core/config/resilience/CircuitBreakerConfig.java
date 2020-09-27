package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

@Data
public class CircuitBreakerConfig {
    private long waitDurationInOpenState = 10_000;
    private int minimumNumberOfCalls = 100;
    private int slidingWindowSize = 100;
    private int permittedNumberOfCallsInHalfOpenState = 100;
    private int slowCallDurationThreshold = 500;
    private int slowCallRateThreshold = 100;
    private int failureRateThreshold = 50;

}
