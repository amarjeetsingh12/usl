package com.flipkart.gap.usl.client.kafka;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProducerConfig {
    private String brokerConnectionString;
    private String zkConnectionString;
    private int producersCount = 1;
    private int requestTimeout = 30000;
    private int maxBlockMS = 60000;
    private int maxIdleTime = 540000;
    private int batchSize = 10000;
    private int lingerTimeInMs = 100;
    private int retry = 0;
}
