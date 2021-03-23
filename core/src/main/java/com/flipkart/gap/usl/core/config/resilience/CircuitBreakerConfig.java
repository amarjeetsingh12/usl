package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

import java.io.Serializable;

@Data
public class CircuitBreakerConfig implements Serializable {
    private long waitDurationInOpenState = 10_000;
    private int minimumNumberOfCalls = 100;
    private int slidingWindowSize = 100;
    private int permittedNumberOfCallsInHalfOpenState = 100;
    private int slowCallDurationThreshold = 500;
    private int slowCallRateThreshold = 100;
    private int failureRateThreshold = 50;

}
