package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

import java.io.Serializable;

@Data
public class RateLimiterConfig implements Serializable {
    private long limitRefreshPeriod = 1000;
    private int limitForPeriod = 1000;
    private long timeoutDuration = 500;
}
