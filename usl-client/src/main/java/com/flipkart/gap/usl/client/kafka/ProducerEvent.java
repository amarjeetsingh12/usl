package com.flipkart.gap.usl.client.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by vinay.lodha on 20/06/17.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class ProducerEvent {
    private String name;
    private JsonNode payload;
}
