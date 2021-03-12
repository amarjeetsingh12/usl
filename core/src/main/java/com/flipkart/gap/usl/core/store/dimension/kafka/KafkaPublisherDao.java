package com.flipkart.gap.usl.core.store.dimension.kafka;

import com.flipkart.gap.usl.client.kafka.KafkaProducerException;
import com.flipkart.gap.usl.core.processor.stage.model.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public abstract class KafkaPublisherDao {

    protected Producer<String, byte[]> producer;

    public void sendEventsSync(List<ProducerRecord<String, byte[]>> producerRecordList) throws KafkaProducerException {
        try {
                List<Future<RecordMetadata>> futureList = producerRecordList.stream().map(producer::send).collect(Collectors.toList());
                List<ProducerRecord> failedRecords = new ArrayList<>();
                Exception failureException = null;
                for (int i = 0; i < futureList.size(); i++) {
                    try {
                        futureList.get(i).get();
                    } catch (InterruptedException | ExecutionException e) {
                        failureException = e;
                        failedRecords.add(producerRecordList.get(i));
                        log.error("Error while sending record for: " + producerRecordList.get(i).toString());
                    }
                }
                if (failedRecords.size() > 0) {
                    throw new KafkaProducerException("No of failed records: " + failedRecords.size(), failureException, failedRecords);
                }
        } catch (Exception e) {
            throw new KafkaProducerException(e);
        }
    }

    public void sendRecords(List<KafkaProducerRecord> producerRecordList) throws KafkaProducerException {
        List<ProducerRecord<String, byte[]>> records = new ArrayList<>();
        producerRecordList.
                forEach(producerRecord ->
                        records.add(new ProducerRecord<>(producerRecord.getTopicName(), producerRecord.getValue())));

        sendEventsSync(records);


    }

}
