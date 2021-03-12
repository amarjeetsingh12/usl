package com.flipkart.gap.usl.core.config;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * Created by amarjeet.singh on 18/10/16.
 */
@Getter
@Setter
public class EventProcessorConfig implements Serializable {
    @NotBlank
    private String sparkMasterWithPort;
    @Min(10)
    private int batchDurationInSeconds = 30;
    @Min(20)
    private int partitions = 30;
    private int perPartitionRecord = 50;
    @Min(10000)
    private int batchSize = 100000;
    @Min(1)
    private int dimensionProcessingBatchSize = 50;
    @NotBlank
    private String topicName;

    @NotBlank
    private String kafkaBrokerConnection;
    private String backPressureInitialRate = "20000";
    @NotBlank
    private String zkHosts;
    @NotBlank
    private int zkPort = 2181;
    @NotBlank
    private String zkRoot = "/";
    private String executorExtraJavaOpts = "";
    private String executorMemory = "1g";
    private int executorCores = 1;
    private String blockInterval = "500ms";
    private String environment;
    private int offsetSaveThreads = 10;
    private int producersCount = 1;
    private int requestTimeout = 30000;
    private int maxBlockMS = 60000;
    private int maxIdleTime = 540000;
    private int lingerTimeInMs = 100;
    private int retry = 0;
    private KafkaConfig kafkaConfig;

    public EventProcessorConfig() {
    }

    @Getter
    @Setter
    public static class KafkaConfig implements Serializable {
        private String groupId = "spark_processor";
        private String autoOffsetReset = "latest";
        private boolean enableAutoCommit = false;
        private int fetchMaxWait = 100;
        private int fetchMinBytes = 1;
        private int heartBeatIntervalMS = 500;
        private int sessionTimeoutMS = 1000;
        private int requestTimeoutMS = 1500;
    }
}
