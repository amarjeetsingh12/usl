package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

@Data
public class TimeLimiterConfig {
    private boolean cancelRunningFuture = true;
    private long timeoutDuration = 500;
}
