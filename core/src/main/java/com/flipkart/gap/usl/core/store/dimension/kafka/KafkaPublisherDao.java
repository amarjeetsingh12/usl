package com.flipkart.gap.usl.core.store.dimension.kafka;

import com.flipkart.gap.usl.core.model.dimension.Dimension;

import java.util.Set;

public interface KafkaPublisherDao {
    void bulkPublish(Set<Dimension> dimensions) throws Exception;
}
