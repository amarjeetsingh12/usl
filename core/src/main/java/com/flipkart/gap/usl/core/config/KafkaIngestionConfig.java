package com.flipkart.gap.usl.core.config;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class KafkaIngestionConfig implements Serializable {

    private String topicName;

    @NotBlank
    private String kafkaBrokerConnection;
    private int offsetSaveThreads = 10;
    private int producersCount = 10;
    private int requestTimeout = 30000;
    @Min(10000)
    private int batchSize = 100000;
    private int maxBlockMS = 60000;
    private int maxIdleTime = 540000;
    private int lingerTimeInMs = 100;
    private int retry = 0;
    private int executorServicePoolSize = 10;
}
