package com.flipkart.gap.usl.core.model.external;

import com.esotericsoftware.kryo.NotNull;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * Created by amarjeet.singh on 19/10/16.
 */
@Getter
@Setter
public class ExternalEventSchema implements Serializable {
    @NotNull @NotBlank
    private String name;
    @NotNull
    private ObjectNode payload;

}
