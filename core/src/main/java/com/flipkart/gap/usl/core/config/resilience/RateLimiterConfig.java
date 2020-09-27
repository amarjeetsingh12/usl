package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

@Data
public class RateLimiterConfig {
    private long limitRefreshPeriod = 1000;
    private int limitForPeriod = 1000;
    private long timeoutDuration = 500;
}
