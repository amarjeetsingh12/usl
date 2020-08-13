package com.flipkart.gap.usl.core.model.dimension;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by amarjeet.singh on 03/10/16.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventSpecs {
    String name();
}
