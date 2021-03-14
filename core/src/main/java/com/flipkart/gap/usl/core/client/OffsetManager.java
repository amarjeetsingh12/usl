package com.flipkart.gap.usl.core.client;

import com.flipkart.gap.usl.core.config.EventProcessorConfig;
import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.exception.OffsetSaveException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.streaming.kafka010.OffsetRange;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by amarjeet.singh on 13/10/16.
 */
@Slf4j
@Singleton
public class OffsetManager {
    private ZooKeeper zooKeeper = null;
    @Inject
    private KafkaClient kafkaClient;
    @Inject
    @Named("eventProcessorConfig")
    private EventProcessorConfig eventProcessorConfig;
    private Map<Integer, Long> offsetMap;
    private ExecutorService executorService;
    private static final int MAX_OFFSET_RETRY = 3;

    public OffsetManager() {
    }

    @Inject
    public void init() throws IOException, InterruptedException, KeeperException {
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        String[] hostList = eventProcessorConfig.getZkHosts().split(",");
        StringBuilder connectionStringBuilder = new StringBuilder();
        for (String host : hostList) {
            connectionStringBuilder.append(String.format("%s:%d,", host, eventProcessorConfig.getZkPort()));
        }
        connectionStringBuilder.append(eventProcessorConfig.getZkRoot());
        zooKeeper = new ZooKeeper(connectionStringBuilder.toString(), 500000, we -> {
            if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown();
            }
        });
        Stat consumerStat = this.zooKeeper.exists(Constants.CONSUMER_PATH, false);
        if (consumerStat == null) {
            this.zooKeeper.create(Constants.CONSUMER_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            this.zooKeeper.create(Constants.OFFSET_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        connectedSignal.await();
        executorService = Executors.newFixedThreadPool(eventProcessorConfig.getOffsetSaveThreads());
    }

    public void saveOffset(OffsetRange[] offsetRanges) throws InterruptedException {
        List<Callable<Boolean>> callables = Arrays.stream(offsetRanges).map(offsetRange -> (Callable<Boolean>) () -> {
            saveOffset(offsetRange.topic(), offsetRange.partition(), offsetRange.untilOffset());
            log.info("Topic {} processed for partition {} ,from offset {}, to offset {}", offsetRange.topic(), offsetRange.partition(), offsetRange.fromOffset(), offsetRange.untilOffset());
            return true;
        }).collect(Collectors.toList());
        executorService.invokeAll(callables).forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new OffsetSaveException(e);
            }
        });
    }

    private void saveOffset(String topicName, int partition, long toOffset) throws OffsetSaveException {
        saveOffset(topicName, partition, toOffset, 0);
    }

    private void saveOffset(String topicName, int partition, long toOffset, int runNumber) throws OffsetSaveException {
        try {
            if (runNumber < MAX_OFFSET_RETRY) {
                reconnectZKIfRequired();
                String topicPath = getTopicPath(topicName);
                Stat topicStat = this.zooKeeper.exists(topicPath, false);
                if (topicStat == null) {
                    this.zooKeeper.create(topicPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
                String partitionPath = getPartitionPath(partition, topicName);
                Stat partitionStat = this.zooKeeper.exists(partitionPath, false);
                if (partitionStat == null) {
                    this.zooKeeper.create(partitionPath, (toOffset + "").getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                } else {
                    this.zooKeeper.setData(partitionPath, (toOffset + "").getBytes(), partitionStat.getVersion());
                }
            } else {
                throw new OffsetSaveException("Retries exhausted");
            }
        } catch (InterruptedException | KeeperException | IOException e) {
            log.error("Error saving offsets ", e);
            zooKeeper = null;
            saveOffset(topicName, partition, toOffset, ++runNumber);
        }
    }

    private void reconnectZKIfRequired() throws InterruptedException, IOException, KeeperException {
        if (zooKeeper == null || zooKeeper.getState() != ZooKeeper.States.CONNECTED) {
            init();
        }
    }

    public Map<TopicPartition, Long> getTopicPartition() throws Exception {
        reconnectZKIfRequired();
        String topicName = eventProcessorConfig.getTopicName();
        int partitionCount = kafkaClient.getPartitionCount();
        Map<TopicPartition, Long> topicPartitionMap = new HashMap<>();
        for (int partition = 0; partition < partitionCount; partition++) {
            Stat partitionStat = this.zooKeeper.exists(getPartitionPath(partition, topicName), false);
            if (partitionStat == null) {
                Long defaultOffset = getEarliestOffset(partition);
                topicPartitionMap.put(new TopicPartition(topicName, partition), defaultOffset);
                log.info("Zookeeper partition stat not found sending earliest {},{},{}", topicName, partition, defaultOffset);
            } else {
                long offsetFound = Long.parseLong(new String(this.zooKeeper.getData(getPartitionPath(partition, topicName), false, partitionStat)));
                topicPartitionMap.put(new TopicPartition(topicName, partition), offsetFound);
                log.info("Zookeeper partition stat found sending existing {},{},{}", topicName, partition, offsetFound);
            }
        }
        return topicPartitionMap;
    }

    public Map<TopicPartition, Long> getMultipleTopicPartitions() throws Exception {
        reconnectZKIfRequired();
        Map<TopicPartition, Long> topicPartitionMap = new HashMap<>();
        List<String> topicNames = eventProcessorConfig.getTopicNames();
        int partitionCount = kafkaClient.getPartitionCount(topicNames);
        for (String topicName: topicNames) {

            for (int partition = 0; partition < partitionCount; partition++) {
                Stat partitionStat = this.zooKeeper.exists(getPartitionPath(partition, topicName), false);
                if (partitionStat == null) {
                    Long defaultOffset = getEarliestOffset(partition, topicName);
                    topicPartitionMap.put(new TopicPartition(topicName, partition), defaultOffset);
                    log.info("Zookeeper partition stat not found sending earliest {},{},{}", topicName, partition, defaultOffset);
                } else {
                    long offsetFound = Long.parseLong(new String(this.zooKeeper.getData(getPartitionPath(partition, topicName), false, partitionStat)));
                    topicPartitionMap.put(new TopicPartition(topicName, partition), offsetFound);
                    log.info("Zookeeper partition stat found sending existing {},{},{}", topicName, partition, offsetFound);
                }
            }

        }


        return topicPartitionMap;
    }

    private Long getEarliestOffset(int partition) throws Exception {
        if (offsetMap == null) {
            offsetMap = kafkaClient.getPartitionOffsets();
        }
        return offsetMap.get(partition);
    }

    private Long getEarliestOffset(int partition, String topicName) throws Exception {
        if (offsetMap == null) {
            offsetMap = kafkaClient.getPartitionOffsets(topicName);
        }
        return offsetMap.get(partition);
    }

    private static String getTopicPath(final String topicName) {
        return String.format("%s/%s", Constants.OFFSET_PATH, topicName);
    }

    private static String getPartitionPath(int partitionNo, String topicName) {
        return String.format("%s/%d", getTopicPath(topicName), partitionNo);
    }

}
