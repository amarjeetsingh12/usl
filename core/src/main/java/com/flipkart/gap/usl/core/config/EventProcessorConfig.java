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
    @NotBlank
    private String checkpointDirectory;
    @NotBlank
    private String zkHosts;
    @NotBlank
    private int zkPort = 2181;
    @NotBlank
    private String zkRoot = "/";
    private String blockInterval = "500ms";
    private String environment;
    private int offsetSaveThreads=10;

    public EventProcessorConfig() {
    }

}
