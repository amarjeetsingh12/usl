package com.flipkart.gap.usl.core.processor;

import com.codahale.metrics.Timer;
import com.flipkart.gap.usl.core.client.KafkaClient;
import com.flipkart.gap.usl.core.client.OffsetManager;
import com.flipkart.gap.usl.core.config.ConfigurationModule;
import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.config.v2.ApplicationConfiguration;
import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.helper.SparkHelper;
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
import lombok.extern.slf4j.Slf4j;
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

/**
 * Created by amarjeet.singh on 18/10/16.
 */
@Slf4j
@Singleton
public class EventStreamProcessor implements Serializable {
    @Inject
    private ApplicationConfiguration applicationConfiguration;
    @Inject
    private transient OffsetManager offsetManager;
    @Inject
    private transient KafkaClient kafkaClient;
    private transient SparkConf sparkConf;
    private transient HashMap<String, Object> kafkaParams;
    private transient JavaStreamingContext javaStreamingContext;

    @Inject
    public void init() {
        log.info("Initialising configs");
        EventProcessorConfig eventProcessorConfig = applicationConfiguration.getEventProcessorConfig();
        sparkConf = new SparkConf().setMaster(eventProcessorConfig.getSparkMasterWithPort()).setAppName(Constants.Stream.GROUP_ID);
        sparkConf.set("spark.streaming.backpressure.initialRate", eventProcessorConfig.getBackPressureInitialRate());
        sparkConf.set("spark.dynamicAllocation.enabled", "false");
        sparkConf.set("spark.streaming.receiver.maxRate", eventProcessorConfig.getBatchSize() + "");
        sparkConf.set("spark.streaming.stopGracefullyOnShutdown", "true");
        sparkConf.set("spark.executor.extraJavaOptions", eventProcessorConfig.getExecutorExtraJavaOpts());
        sparkConf.set("spark.executor.cores", eventProcessorConfig.getExecutorCores() + "");
        sparkConf.set("spark.executor.memory", eventProcessorConfig.getExecutorMemory());
        sparkConf.set("spark.job.interruptOnCancel", "true");
        int maxRate = eventProcessorConfig.getBatchSize();
        log.info("fetching partition count configs");
        int partitionCount = kafkaClient.getPartitionCount();
        log.info("fetched {} partition count configs",partitionCount);
        int maxRatePerPartition = maxRate / (partitionCount * eventProcessorConfig.getBatchDurationInSeconds());
        sparkConf.set("spark.streaming.kafka.maxRatePerPartition", maxRatePerPartition + "");
        log.info("Using spark config {}", sparkConf);
        kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", eventProcessorConfig.getKafkaBrokerConnection());
        kafkaParams.put("key.deserializer", ByteArrayDeserializer.class);
        kafkaParams.put("value.deserializer", ByteArrayDeserializer.class);
        kafkaParams.put("group.id", eventProcessorConfig.getKafkaConfig().getGroupId());
        kafkaParams.put("auto.offset.reset", eventProcessorConfig.getKafkaConfig().getAutoOffsetReset());
        kafkaParams.put("enable.auto.commit", eventProcessorConfig.getKafkaConfig().isEnableAutoCommit());
        kafkaParams.put("fetch.max.wait.ms", eventProcessorConfig.getKafkaConfig().getFetchMaxWait());
        kafkaParams.put("fetch.min.bytes", eventProcessorConfig.getKafkaConfig().getFetchMinBytes());
        kafkaParams.put("heartbeat.interval.ms", eventProcessorConfig.getKafkaConfig().getHeartBeatIntervalMS());
        kafkaParams.put("session.timeout.ms", eventProcessorConfig.getKafkaConfig().getSessionTimeoutMS());
        kafkaParams.put("request.timeout.ms", eventProcessorConfig.getKafkaConfig().getRequestTimeoutMS());
        log.info("Using kafka params config {}", sparkConf);
    }

    public void process() throws ProcessingException {
        try {
            javaStreamingContext = getStreamingContext(applicationConfiguration.getEventProcessorConfig());
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

        messages.foreachRDD((VoidFunction2<JavaRDD<ConsumerRecord<byte[], byte[]>>, Time>) (consumerRecordJavaRDD, v2) -> {
            OffsetRange[] offsets = ((HasOffsetRanges) consumerRecordJavaRDD.rdd()).offsetRanges();
            offsetRanges.set(offsets);
            for (OffsetRange offsetRange : offsets) {
                log.info("Started Batch processing with offsets {},{},{},{}", offsetRange.topic(), offsetRange.partition(), offsetRange.fromOffset(), offsetRange.untilOffset());
            }
        });

        messages.foreachRDD(
                (VoidFunction2<JavaRDD<ConsumerRecord<byte[], byte[]>>, Time>) (consumerRecordJavaRDD, time) -> {
                    JavaPairRDD<EntityDimensionCompositeKey, Iterable<InternalEventMeta>> internalEventMetaRDD = consumerRecordJavaRDD.flatMapToPair((PairFlatMapFunction<ConsumerRecord<byte[], byte[]>, EntityDimensionCompositeKey, InternalEventMeta>) consumerRecord -> {
                        SparkHelper.bootstrap();
                        try (Timer.Context context = JmxReporterMetricRegistry.getInstance().getEventParsingTimer().time()) {
                            ExternalEventHelper externalEventHelper = ConfigurationModule.getInjector(applicationConfiguration).getInstance(ExternalEventHelper.class);
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
                                    SparkHelper.bootstrap();
                                    ExternalEventHelper externalEventHelper = ConfigurationModule.getInjector(applicationConfiguration).getInstance(ExternalEventHelper.class);
                                    return externalEventHelper.createSubListPartitions(groupedPartitionIterator, eventProcessorConfig.getDimensionProcessingBatchSize()).stream().map(ProcessingStageData::new).collect(Collectors.toList()).iterator();
                                }
                                return Collections.emptyIterator();
                            }
                    );
                    JavaRDD<ProcessingStageData> fetchedRDD = batchedRDD.map(dimensionFetchRequest -> {
                        SparkHelper.bootstrap();
                        DimensionFetchStage dimensionFetchStage = ConfigurationModule.getInjector(applicationConfiguration).getInstance(DimensionFetchStage.class);
                        dimensionFetchStage.execute(dimensionFetchRequest);
                        return dimensionFetchRequest;
                    });
                    JavaRDD<ProcessingStageData> processRDD = fetchedRDD.map(dimensionProcessRequest -> {
                        SparkHelper.bootstrap();
                        DimensionProcessStage dimensionProcessStage = ConfigurationModule.getInjector(applicationConfiguration).getInstance(DimensionProcessStage.class);
                        dimensionProcessStage.execute(dimensionProcessRequest);
                        return dimensionProcessRequest;
                    });
                    JavaRDD<ProcessingStageData> persistedRDD = processRDD.map(dimensionPersistRequest -> {
                        SparkHelper.bootstrap();
                        DimensionSaveStage dimensionSaveStage = ConfigurationModule.getInjector(applicationConfiguration).getInstance(DimensionSaveStage.class);
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
