package com.flipkart.gap.usl.core.store.dimension.kafka;

import com.codahale.metrics.Timer;
import com.flipkart.gap.usl.core.config.KafkaIngestionConfig;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.processor.stage.model.KafkaProducerRecord;
import com.flipkart.gap.usl.core.store.exception.KafkaProducerException;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.record.CompressionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class KafkaPublisherDao {

    public static final String KAFKA_PRODUCER_WAIT = "kafkaProducerWait";
    public static final String KAFKA_SEND_EVENTS_SYNC = "kafkaSendEventsSync";
    public static final String KAFKA_SEND_BATCH = "kafkaSendBatch";
    private static final String executorSvsErrorMessage = "Tried using Executor service but is already shutdown or not initiated. Can't continue. Check eventProcessorConfig";

    protected LinkedBlockingQueue<Producer<String, byte[]>> producers;
    protected ExecutorService executorServicePool;
    protected Properties props;

    @Inject
    @Named("kafkaIngestionConfig")
    private KafkaIngestionConfig kafkaIngestionConfig;
    @Inject
    public void init() {
        props = new Properties();

        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaIngestionConfig.getKafkaBrokerConnection());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG, kafkaIngestionConfig.getRetry());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BATCH_SIZE_CONFIG, kafkaIngestionConfig.getBatchSize());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, kafkaIngestionConfig.getLingerTimeInMs());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaIngestionConfig.getRequestTimeout());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.MAX_BLOCK_MS_CONFIG, kafkaIngestionConfig.getMaxBlockMS());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, kafkaIngestionConfig.getMaxIdleTime());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.GZIP.name);


        try {
            producers = new LinkedBlockingQueue<>(kafkaIngestionConfig.getProducersCount());
            for (int i = 0; i < kafkaIngestionConfig.getProducersCount(); i++) {
                producers.add(new KafkaProducer<>(props));
            }
            if (kafkaIngestionConfig.getExecutorServicePoolSize() != 0) {
                executorServicePool = Executors.newFixedThreadPool(kafkaIngestionConfig.getExecutorServicePoolSize());
            }
        } catch (Exception e) {
            log.error("Exception making producer: {}", ExceptionUtils.getMessage(e), e);
        }
    }
    /*
    Nested Callable class
     */
    private class KafkaSendBatch implements Callable<Boolean> {

        private List<ProducerRecord<String, byte[]>> producerRecords;

        KafkaSendBatch(List<ProducerRecord<String, byte[]>> producerRecordList) {
            this.producerRecords = producerRecordList;
        }

        @Override
        public Boolean call() {
            try (Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(KAFKA_SEND_BATCH).time()) {
                sendRecordsSync(producerRecords);
                return true;
            } catch (KafkaProducerException ke) {
                log.error("Number of retries: {} exhausted. Throwing exception.");
                throw ke;
            }
        }
    }

    private void sendRecordsSync(List<ProducerRecord<String, byte[]>> producerRecordList) throws KafkaProducerException {
        try {
            Producer<String, byte[]> producer = getProducer();
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
            } finally {
                returnProducer(producer);
            }
        } catch (InterruptedException e) {
            throw new KafkaProducerException(e);
        }
    }

    private List<ProducerRecord<String, byte[]>> getProducerRecords(List<KafkaProducerRecord> producerRecordList) throws KafkaProducerException {
        List<ProducerRecord<String, byte[]>> records = new ArrayList<>();
        producerRecordList.
                forEach(producerRecord ->
                        records.add(new ProducerRecord<>(producerRecord.getTopicName(), producerRecord.getKey(), producerRecord.getValue())));

        return records;
    }

    public void sendRecords(List<KafkaProducerRecord> producerRecordList) throws KafkaProducerException {

        List<ProducerRecord<String, byte[]>> kafkaProducerRecords = getProducerRecords(producerRecordList);

        try (Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(KAFKA_SEND_EVENTS_SYNC).time()) {
            if (null != executorServicePool && !executorServicePool.isShutdown()) {
                List<List<ProducerRecord<String, byte[]>>> producerRecordListOfSubList = Lists.partition(kafkaProducerRecords, (Integer)props.get(org.apache.kafka.clients.producer.ProducerConfig.BATCH_SIZE_CONFIG));
                List<Future<Boolean>> futureListOfKafkaSendTasks = producerRecordListOfSubList
                        .stream()
                        .map((subList) -> executorServicePool.submit(new KafkaSendBatch(subList)))
                        .collect(Collectors.toList());

                for (Future<Boolean> futureKafkaSendTask : futureListOfKafkaSendTasks) {
                    try {
                        futureKafkaSendTask.get();
                    } catch (Exception e) {
                        log.error("Exception while sending kafka events executor service for batch size: {}",  props.get(org.apache.kafka.clients.producer.ProducerConfig.BATCH_SIZE_CONFIG), e);
                        throw new KafkaProducerException("Exception while sending kafka events executor service", e);
                    }
                }
            } else {
                log.error(executorSvsErrorMessage);
                throw new KafkaProducerException(executorSvsErrorMessage);
            }
        }
    }

    private Producer<String, byte[]> getProducer() throws InterruptedException {
        try (Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(KAFKA_PRODUCER_WAIT).time()) {
            return this.producers.poll(10, TimeUnit.SECONDS);
        }
    }

    private void returnProducer(Producer<String, byte[]> producer) throws KafkaProducerException {
        try {
            this.producers.put(producer);
        } catch (InterruptedException e) {
            throw new KafkaProducerException(e);
        }
    }

    public void tearDown() {
        if (null != executorServicePool) {
            executorServicePool.shutdown();
        }

        for (Producer producer : producers) {
            producer.flush();
            producer.close();
        }
    }
}