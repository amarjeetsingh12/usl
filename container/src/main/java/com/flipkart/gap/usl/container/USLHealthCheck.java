package com.flipkart.gap.usl.container;

import com.codahale.metrics.health.HealthCheck;

/**
 * Created by ajaysingh on 05/10/16.
 */
public class USLHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}