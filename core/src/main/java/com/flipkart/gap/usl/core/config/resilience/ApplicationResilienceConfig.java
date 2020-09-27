package com.flipkart.gap.usl.core.config.resilience;

import lombok.Data;

@Data
public class ApplicationResilienceConfig {
    private ResilienceConfig dimensionReadConfig;
    private ResilienceConfig dimensionBulkReadConfig;
    private ResilienceConfig dimensionBulkSaveConfig;
    private ResilienceConfig dimensionDeleteConfig;
}
