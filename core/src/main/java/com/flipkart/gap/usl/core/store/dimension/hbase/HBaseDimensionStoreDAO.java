package com.flipkart.gap.usl.core.store.dimension.hbase;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.flipkart.gap.usl.core.config.HbaseConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.dimension.Dimension;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpecs;
import com.flipkart.gap.usl.core.store.dimension.DimensionDBRequest;
import com.flipkart.gap.usl.core.store.dimension.DimensionStoreDAO;
import com.flipkart.gap.usl.core.store.dimension.resilence.DecoratorExecutionException;
import com.flipkart.gap.usl.core.store.dimension.resilence.ResilenceDecorator;
import com.flipkart.gap.usl.core.store.exception.DimensionDeleteException;
import com.flipkart.gap.usl.core.store.exception.DimensionFetchException;
import com.flipkart.gap.usl.core.store.exception.DimensionPersistException;
import com.flipkart.gap.usl.core.store.exception.DimensionPersistRuntimeException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Singleton
public class HBaseDimensionStoreDAO implements DimensionStoreDAO {
    @Inject
    @Named("hbaseConfig")
    private HbaseConfig hbaseConfig;
    private Connection connection;
    private RowKeyDistributorByHashPrefix keyDistributor;
    private TableName tableName;
    private static ExecutorService execService;
    @Inject
    private ResilenceDecorator resilenceDecorator;
    private static final String GET_DIMENSION_TIMER = "getDimension";
    private static final String GET_BULK_DIMENSION_TIMER = "getBulkDimensions";
    private static final String PUT_BULK_DIMENSION_TIMER = "putBulkDimensions";
    private static final String DELETE_DIMENSION_TIMER = "deletedDimension";
    public static final String DIMENSION_READ_SERVICE = "dimensionReadConfig";
    public static final String DIMENSION_BULK_READ_SERVICE = "dimensionBulkReadConfig";
    public static final String DIMENSION_BULK_SAVE_SERVICE = "dimensionBulkSaveConfig";
    public static final String DIMENSION_DELETE_SERVICE = "dimensionDeleteConfig";

    @Inject
    public void init() throws IOException {
        keyDistributor = new RowKeyDistributorByHashPrefix(new MurmurHash(2000, 0));
        connection = ConnectionFactory.createConnection(getConfiguration());
        tableName = TableName.valueOf(hbaseConfig.getTableName());
        execService = Executors.newFixedThreadPool(hbaseConfig.getExecutorPoolSize());
    }

    private Configuration getConfiguration() {
        Configuration config = HBaseConfiguration.create();
        config.set(HConstants.ZOOKEEPER_QUORUM, hbaseConfig.getZookeeperQuorum());
        config.setInt(HConstants.ZOOKEEPER_CLIENT_PORT, hbaseConfig.getZookeeperClientPort());
        config.set(HConstants.ZOOKEEPER_ZNODE_PARENT, hbaseConfig.getParentNode());
        config.setInt(HConstants.HBASE_RPC_TIMEOUT_KEY, hbaseConfig.getSoConnect());
        config.setInt(HConstants.HBASE_RPC_READ_TIMEOUT_KEY, hbaseConfig.getSoRead());
        config.setInt(HConstants.HBASE_CLIENT_RETRIES_NUMBER, hbaseConfig.getRetryCount());
        config.setInt(HConstants.HBASE_CLIENT_OPERATION_TIMEOUT, hbaseConfig.getClientOperationTimeout());
        config.setInt(HConstants.HBASE_RPC_WRITE_TIMEOUT_KEY, hbaseConfig.getSoWrite());
        config.setInt(HConstants.HBASE_CLIENT_IPC_POOL_SIZE, hbaseConfig.getIpcPoolSize());
        return config;
    }

    private Table getTable() throws IOException {
        return connection.getTable(tableName, execService);
    }

    @Override
    public <T extends Dimension> T getDimension(DimensionDBRequest dimensionReadDBRequest) throws DimensionFetchException {
        try (Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(GET_DIMENSION_TIMER).time()) {
            return resilenceDecorator.execute(DIMENSION_READ_SERVICE, () -> {
                Get getOp = getFetchOp(dimensionReadDBRequest);
                try (Table table = getTable()) {
                    Result result = table.get(getOp);
                    return readResult(result, dimensionReadDBRequest);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (DecoratorExecutionException e) {
            throw new DimensionFetchException(e);
        }
    }

    private Get getFetchOp(DimensionDBRequest dimensionReadDBRequest) {
        DimensionSpecs dimensionSpecs = dimensionReadDBRequest.getDimensionClass().getAnnotation(DimensionSpecs.class);
        byte[] distributedKey = getDistributedKey(getRowKey(dimensionReadDBRequest.getEntityId(), dimensionSpecs.name(), dimensionReadDBRequest.getVersion()));
        Get getOp = new Get(distributedKey);
        getOp.addColumn(HBaseConstants.CF, HBaseConstants.CQ);
        return getOp;
    }

    private <T extends Dimension> T readResult(Result result, DimensionDBRequest dimensionDBRequest) throws IOException {
        if (result != null) {
            NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(HBaseConstants.CF);
            if (familyMap != null && familyMap.containsKey(HBaseConstants.CQ)) {
                byte[] dataBytes = familyMap.get(HBaseConstants.CQ);
                return ObjectMapperFactory.getMapper().readValue(dataBytes, ((Class<T>) (dimensionDBRequest.getDimensionClass())));
            }
        }
        return null;
    }

    public Map<DimensionDBRequest, Dimension> bulkGet(Set<DimensionDBRequest> dimensionReadDBRequests) throws DimensionFetchException {
        Map<String, DimensionDBRequest> rowKeyMap = new HashMap<>();
        Map<DimensionDBRequest, Dimension> responseMap = new HashMap<>();
        List<Get> getOps = new ArrayList<>();
        for (DimensionDBRequest dimensionDBRequest : dimensionReadDBRequests) {
            Get fetchOp = this.getFetchOp(dimensionDBRequest);
            getOps.add(fetchOp);
            rowKeyMap.put(new String(fetchOp.getRow()), dimensionDBRequest);
        }
        try (Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(GET_BULK_DIMENSION_TIMER).time()) {
            return resilenceDecorator.execute(DIMENSION_BULK_READ_SERVICE, () -> {
                try (Table table = getTable()) {
                    Result[] results = table.get(getOps);
                    for (Result result : results) {
                        if (!result.isEmpty()) {
                            byte[] row = result.getRow();
                            DimensionDBRequest dimensionDBRequest = rowKeyMap.get(new String(row));
                            responseMap.put(dimensionDBRequest, readResult(result, dimensionDBRequest));
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return responseMap;
            });
        } catch (DecoratorExecutionException e) {
            throw new DimensionFetchException(e);
        }

    }

    private Put getPut(Dimension dimension) throws JsonProcessingException {
        DimensionSpecs dimensionSpecs = dimension.getClass().getAnnotation(DimensionSpecs.class);
        byte[] distributedKey = getDistributedKey(getRowKey(dimension.getEntityId(), dimensionSpecs.name(), dimension.getVersion()));
        Put put = new Put(distributedKey);
        put.addColumn(HBaseConstants.CF, HBaseConstants.CQ, ObjectMapperFactory.getMapper().writeValueAsBytes(dimension));
        return put;
    }

    public void bulkSave(Set<Dimension> dimensions) throws DimensionPersistRuntimeException, DimensionPersistException {
        List<Put> puts = new ArrayList<>();
        for (Dimension dimension : dimensions) {
            try {
                Put put = getPut(dimension);
                puts.add(put);
            } catch (JsonProcessingException e) {
                throw new DimensionPersistException("Unable to process json", e);
            }
        }
        try (Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(PUT_BULK_DIMENSION_TIMER).time()) {
            resilenceDecorator.execute(DIMENSION_BULK_SAVE_SERVICE, () -> {
                try (Table table = getTable()) {
                    table.put(puts);
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException("Unable to get table", e);
                }
            });
        } catch (DecoratorExecutionException e) {
            throw new DimensionPersistException(e);
        }
    }


    @Override
    public void deleteDimension(DimensionDBRequest dimensionReadDBRequest) throws DimensionDeleteException {
        try (Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(DELETE_DIMENSION_TIMER).time()) {
            resilenceDecorator.execute(DIMENSION_DELETE_SERVICE, () -> {
                try (Table table = getTable()) {
                    DimensionSpecs dimensionSpecs = dimensionReadDBRequest.getDimensionClass().getAnnotation(DimensionSpecs.class);
                    byte[] distributedKey = getDistributedKey(getRowKey(dimensionReadDBRequest.getEntityId(), dimensionSpecs.name(), dimensionReadDBRequest.getVersion()));
                    table.delete(new Delete(distributedKey));
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException("Unable to get table", e);
                }
            });
        } catch (DecoratorExecutionException e) {
            throw new DimensionDeleteException(e);
        }
    }

    private String getRowKey(String accountId, String dimensionName, int version) {
        return String.format("%s-%s-%d", accountId, dimensionName, version);
    }


    private byte[] getDistributedKey(String rowKey) {
        return keyDistributor.getDistributedKey(Bytes.toBytes(rowKey));
    }
}

