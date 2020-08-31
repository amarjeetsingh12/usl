package com.flipkart.gap.usl.core.config;

import lombok.Data;

import java.io.Serializable;

@Data
public class MongoConfig implements Serializable {
    private String dbName;
    private String connectionString;
    private int requestTimeout=10000;
    private int connectionTimeout = 3000;
    private int connectionsPerHost = 3;
}
