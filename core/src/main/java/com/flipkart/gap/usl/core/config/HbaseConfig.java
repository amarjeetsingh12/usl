package com.flipkart.gap.usl.core.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class HbaseConfig implements Serializable {
    private String tableName;
    private String zookeeperQuorum;
    private int zookeeperClientPort = 2181;
    private String parentNode;
    private int soConnect = 10000;
    private int soRead = 2000;
    private int soWrite = 2000;
    private int ipcPoolSize = 1;
    private int executorPoolSize = 100;
}
