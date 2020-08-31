package com.flipkart.gap.usl.core.config;

import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * Created by ankesh.maheshwari on 19/10/16.
 */
@Getter
public class CacheConfig implements Serializable {
    @Min(5)
    @Max(60)
    private int ttlInMinutes = 30;
}
