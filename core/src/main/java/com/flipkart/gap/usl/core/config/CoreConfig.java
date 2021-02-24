package com.flipkart.gap.usl.core.config;

import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.List;

/**
 * Created by ankesh.maheshwari on 19/10/16.
 */
@Getter
public class CoreConfig implements Serializable {

    private List<String> dimensionsWithPublishEvents;

}
