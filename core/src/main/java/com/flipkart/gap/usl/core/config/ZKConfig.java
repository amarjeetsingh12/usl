package com.flipkart.gap.usl.core.config;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by amarjeet.singh on 21/10/16.
 */
@Getter
@Setter
public class ZKConfig {
    @NotNull
    @NotEmpty
    private String zookeperConnectionString;
}
