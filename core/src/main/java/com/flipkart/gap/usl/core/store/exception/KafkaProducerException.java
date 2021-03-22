package com.flipkart.gap.usl.core.store.exception;

import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayList;
import java.util.List;

@Getter
public class KafkaProducerException extends RuntimeException {

    private List<ProducerRecord> failedRecords = new ArrayList<>();

    public KafkaProducerException(String message) {
        super(message);
    }

    public KafkaProducerException(Throwable cause) {
        super(cause);
    }

    public KafkaProducerException(String message, Throwable cause) {
        super(message,cause);
    }

    public KafkaProducerException(String message, List<ProducerRecord> failedRecords) {
        super(message);
        this.failedRecords = failedRecords;
    }

    public KafkaProducerException(String message, Throwable cause,  List<ProducerRecord> failedRecords) {
        super(message, cause);
        this.failedRecords = failedRecords;
    }
}