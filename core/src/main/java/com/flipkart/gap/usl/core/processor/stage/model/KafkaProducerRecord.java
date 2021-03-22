package com.flipkart.gap.usl.core.processor.stage.model;

import com.flipkart.gap.usl.core.model.DimensionMutateRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KafkaProducerRecord implements Serializable {

    private String topicName;
    private String key;
    private byte[] value;
}
