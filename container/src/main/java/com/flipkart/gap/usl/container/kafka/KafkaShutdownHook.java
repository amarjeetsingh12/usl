package com.flipkart.gap.usl.container.kafka;

import com.flipkart.gap.usl.client.kafka.KafkaProducerClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by ashish.khatkar on 15/03/18.
 */
@Slf4j
@Singleton
public class KafkaShutdownHook implements Managed {
    @Inject
    private KafkaProducerClient kafkaProducerClient;

    @Override
    public void start() throws Exception {
        // do nothing
    }

    @Override
    public void stop() throws Exception {
        log.info("Starting kafka shutdown");
        kafkaProducerClient.tearDown();
        log.info("Kafka shutdown done");
    }
}
