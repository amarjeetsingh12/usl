package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

import java.io.Serializable;

@Data
public class TimeLimiterConfig implements Serializable {
    private boolean cancelRunningFuture = true;
    private long timeoutDuration = 500;
}
