package com.flipkart.gap.usl.core.processor;

import com.codahale.metrics.Timer;
import com.flipkart.gap.usl.core.client.KafkaClient;
import com.flipkart.gap.usl.core.client.OffsetManager;
import com.flipkart.gap.usl.core.config.ConfigurationModule;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.helper.Helper;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.EntityDimensionCompositeKey;
import com.flipkart.gap.usl.core.model.InternalEventMeta;
import com.flipkart.gap.usl.core.processor.exception.ProcessingException;
import com.flipkart.gap.usl.core.processor.stage.DimensionFetchStage;
import com.flipkart.gap.usl.core.processor.stage.DimensionProcessStage;
import com.flipkart.gap.usl.core.processor.stage.DimensionSaveStage;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.flipkart.gap.usl.core.constant.Constants.CLIENT_NAME_PROPERTY_IDENTIFIER;
import static com.flipkart.gap.usl.core.constant.Constants.ENVIRONMENT_NAME_PROPERTY_IDENTIFIER;

/**
 * Created by amarjeet.singh on 18/10/16.
 */
@Slf4j
@Singleton
public class EventStreamProcessor implements Serializable {
    @Inject
    @Named("eventProcessorConfig")
    private EventProcessorConfig eventProcessorConfig;
    @Inject
    private OffsetManager offsetManager;
    @Inject
    private KafkaClient kafkaClient;

    @Inject
    private ConfigurationModule configurationModule;

    private SparkConf sparkConf;
    private HashMap<String, Object> kafkaParams;
    private transient JavaStreamingContext javaStreamingContext;

    @Inject
    @Named("clientId")
    private String clientId;

    @Inject
    @Named("env")
    private String env;

    @Inject
    public void init() {
        log.info("Initialising configs");
        sparkConf = new SparkConf().setMaster(eventProcessorConfig.getSparkMasterWithPort()).setAppName(String.format("%s-%s-%s", eventProcessorConfig.getEnvironment(), Constants.Stream.GROUP_ID, clientId));
        sparkConf.set("spark.streaming.backpressure.initialRate", "20000");
        sparkConf.set("spark.dynamicAllocation.enabled", "false");
        sparkConf.set("spark.streaming.receiver.maxRate", eventProcessorConfig.getBatchSize() + "");
        sparkConf.set("spark.streaming.kafka.consumer.poll.ms", "100000");
        sparkConf.set("spark.job.interruptOnCancel", "true");
        int maxRate = eventProcessorConfig.getBatchSize();
        int partitionCount = kafkaClient.getPartitionCount();
        int maxRatePerPartition = maxRate / (partitionCount * eventProcessorConfig.getBatchDurationInSeconds());
        sparkConf.set("spark.streaming.kafka.maxRatePerPartition", maxRatePerPartition + "");
        log.info("Using spark config {}", sparkConf);
        kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", eventProcessorConfig.getKafkaBrokerConnection());
        kafkaParams.put("key.deserializer", ByteArrayDeserializer.class);
        kafkaParams.put("value.deserializer", ByteArrayDeserializer.class);
        kafkaParams.put("group.id", "usl_spark");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);
        kafkaParams.put("fetch.max.wait.ms", 100);
        kafkaParams.put("fetch.min.bytes", 1);
        kafkaParams.put("heartbeat.interval.ms", 500);
        kafkaParams.put("session.timeout.ms", 1000);
        kafkaParams.put("request.timeout.ms", 1500);
        if (!env.equals(Constants.LOCAL_ENVIRONEMT)) {
            setSystemProperties(sparkConf);
        }
        log.info("Using kafka params config {}", sparkConf);
    }

    private void setSystemProperties(SparkConf sparkConf) {
        String executorExtraJavaOptions = sparkConf.get("spark.executor.extraJavaOptions");
        if (!StringUtils.isBlank(executorExtraJavaOptions)) {
            executorExtraJavaOptions = String.format("%s -D%s=%s -D%s=%s", executorExtraJavaOptions,
                    ENVIRONMENT_NAME_PROPERTY_IDENTIFIER, env,
                    CLIENT_NAME_PROPERTY_IDENTIFIER, clientId);
        } else {
            executorExtraJavaOptions = String.format("-D%s=%s",
                    ENVIRONMENT_NAME_PROPERTY_IDENTIFIER, env,
                    CLIENT_NAME_PROPERTY_IDENTIFIER, clientId);
        }
        sparkConf.set("spark.executor.extraJavaOptions", executorExtraJavaOptions);
    }

    public void process() throws ProcessingException {
        try {
            javaStreamingContext = getStreamingContext(eventProcessorConfig);
            javaStreamingContext.start();
            javaStreamingContext.awaitTermination();
        } catch (Throwable e) {
            throw new ProcessingException(e);
        }
    }

    private JavaStreamingContext getStreamingContext(EventProcessorConfig eventProcessorConfig) throws Exception {

        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(eventProcessorConfig.getBatchDurationInSeconds()));

        Map<TopicPartition, Long> topicPartitionMap = offsetManager.getTopicPartition();
        log.info("Fetched topic and partition map as {}", topicPartitionMap);
        List<TopicPartition> topicPartitionList = new ArrayList<>(topicPartitionMap.keySet());
        JavaInputDStream<ConsumerRecord<byte[], byte[]>> messages = KafkaUtils.createDirectStream(
                javaStreamingContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Assign(topicPartitionList, kafkaParams, topicPartitionMap)
        );

        /*
          This is to track offset ranges to persist it later in zookeeper.
         */

        final AtomicReference<OffsetRange[]> offsetRanges = new AtomicReference<>();
        /*

          Store offsets right after getting kafka stream.
          Transform input events to dimension update events, create a tuple of composite key (entityId, dimensionName) and dimensionUpdate Event.

         */
        log.info("Starting transform to save offsets ");

        messages.foreachRDD(new VoidFunction2<JavaRDD<ConsumerRecord<byte[], byte[]>>, Time>() {
            @Override
            public void call(JavaRDD<ConsumerRecord<byte[], byte[]>> consumerRecordJavaRDD, Time v2) throws Exception {
                OffsetRange[] offsets = ((HasOffsetRanges) consumerRecordJavaRDD.rdd()).offsetRanges();
                offsetRanges.set(offsets);
                for (OffsetRange offsetRange : offsets) {
                    log.info("Started Batch processing with offsets {},{},{},{}", offsetRange.topic(), offsetRange.partition(), offsetRange.fromOffset(), offsetRange.untilOffset());
                }
            }
        });

        messages.foreachRDD(
                (VoidFunction2<JavaRDD<ConsumerRecord<byte[], byte[]>>, Time>) (consumerRecordJavaRDD, time) -> {
                    JavaPairRDD<EntityDimensionCompositeKey, Iterable<InternalEventMeta>> internalEventMetaRDD = consumerRecordJavaRDD.flatMapToPair((PairFlatMapFunction<ConsumerRecord<byte[], byte[]>, EntityDimensionCompositeKey, InternalEventMeta>) consumerRecord -> {
                        String client = System.getProperty(CLIENT_NAME_PROPERTY_IDENTIFIER);
                        String environment = System.getProperty(ENVIRONMENT_NAME_PROPERTY_IDENTIFIER);
                        Helper.initializeRegisteries(client, environment);
                        try(Timer.Context context = JmxReporterMetricRegistry.getInstance().getEventParsingTimer().time()) {
                            Helper.initializeClients();
                            ExternalEventHelper externalEventHelper = configurationModule.getInjector(client, environment).getInstance(ExternalEventHelper.class);
                            return externalEventHelper.findInternalEventsForExternalEvent(consumerRecord.value());
                        } catch (Throwable throwable) {
                            log.error("Error processing external event {}", new String(consumerRecord.value()), throwable);
                            return Collections.<Tuple2<EntityDimensionCompositeKey, InternalEventMeta>>emptyList().iterator();
                        }
                    })
                            .groupByKey(eventProcessorConfig.getPartitions());

                    JavaRDD<ProcessingStageData> batchedRDD = internalEventMetaRDD.mapPartitions((FlatMapFunction<Iterator<Tuple2<EntityDimensionCompositeKey, Iterable<InternalEventMeta>>>, ProcessingStageData>)
                            groupedPartitionIterator -> {
                                if (groupedPartitionIterator.hasNext()) {
                                    String client = System.getProperty(CLIENT_NAME_PROPERTY_IDENTIFIER);
                                    String environment = System.getProperty(ENVIRONMENT_NAME_PROPERTY_IDENTIFIER);
                                    Helper.initializeRegisteries(client, environment);
                                    Helper.initializeClients();
                                    ExternalEventHelper externalEventHelper = configurationModule.getInjector(client, environment).getInstance(ExternalEventHelper.class);
                                    return externalEventHelper.createSubListPartitions(groupedPartitionIterator, eventProcessorConfig.getDimensionProcessingBatchSize()).stream().map(ProcessingStageData::new).collect(Collectors.toList()).iterator();
                                }
                                return Collections.emptyIterator();
                            }
                    );
                    JavaRDD<ProcessingStageData> fetchedRDD = batchedRDD.map(dimensionFetchRequest -> {
                        String client = System.getProperty(CLIENT_NAME_PROPERTY_IDENTIFIER);
                        String environment = System.getProperty(ENVIRONMENT_NAME_PROPERTY_IDENTIFIER);
                        Helper.initializeRegisteries(client, environment);
                        Helper.initializeClients();
                        DimensionFetchStage dimensionFetchStage = configurationModule.getInjector(client, environment).getInstance(DimensionFetchStage.class);
                        dimensionFetchStage.execute(dimensionFetchRequest);
                        return dimensionFetchRequest;
                    });
                    JavaRDD<ProcessingStageData> processRDD = fetchedRDD.map(dimensionProcessRequest -> {
                        String client = System.getProperty(CLIENT_NAME_PROPERTY_IDENTIFIER);
                        String environment = System.getProperty(ENVIRONMENT_NAME_PROPERTY_IDENTIFIER);
                        Helper.initializeRegisteries(client, environment);
                        Helper.initializeClients();
                        DimensionProcessStage dimensionProcessStage = configurationModule.getInjector(client, environment).getInstance(DimensionProcessStage.class);
                        dimensionProcessStage.execute(dimensionProcessRequest);
                        return dimensionProcessRequest;
                    });
                    JavaRDD<ProcessingStageData> persistedRDD = processRDD.map(dimensionPersistRequest -> {
                        String client = System.getProperty(CLIENT_NAME_PROPERTY_IDENTIFIER);
                        String environment = System.getProperty(ENVIRONMENT_NAME_PROPERTY_IDENTIFIER);
                        Helper.initializeRegisteries(client, environment);
                        Helper.initializeClients();
                        DimensionSaveStage dimensionSaveStage = configurationModule.getInjector(client, environment).getInstance(DimensionSaveStage.class);
                        dimensionSaveStage.execute(dimensionPersistRequest);
                        return dimensionPersistRequest;
                    });
                    try {
                        log.info("Processed {} records in current batch ", persistedRDD.count());
                        internalEventMetaRDD.unpersist();
                        batchedRDD.unpersist();
                    } catch (Throwable throwable) {
                        log.error("Exception occurred during count ", throwable);
                        javaStreamingContext.stop(true, false);
                        System.exit(0);
                    }
                });

        /*
         * This will group all the input events on entityId,dimensionName combination.
         */
        messages.foreachRDD((VoidFunction2<JavaRDD<ConsumerRecord<byte[], byte[]>>, Time>) (v1, v2) -> {
            try {
                offsetManager.saveOffset(offsetRanges.get());
            } catch (Throwable throwable) {
                log.error("Exception occurred during offset save", throwable);
                javaStreamingContext.stop(true, false);
                System.exit(0);
            }
        });

        return javaStreamingContext;
    }
}
