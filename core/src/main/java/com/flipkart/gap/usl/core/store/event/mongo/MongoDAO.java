package com.flipkart.gap.usl.core.store.event.mongo;

import com.flipkart.gap.usl.core.config.MongoConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoDAO {
    private MongoClient mongoClient;
    private MongoConfig mongoConfig;
    
    public MongoDAO(MongoConfig mongoConfig) {
        List<ServerAddress> serverAddresses = new ArrayList<>();
        String[] hosts = mongoConfig.getConnectionString().split(",");
        for (String host : hosts) {
            String[] parts = host.split(":");
            serverAddresses.add(new ServerAddress(parts[0], Integer.parseInt(parts[1])));
        }
        this.mongoClient = new MongoClient(serverAddresses, MongoClientOptions.builder()
                .connectTimeout(mongoConfig.getConnectionTimeout())
                .socketTimeout(mongoConfig.getRequestTimeout())
                .connectionsPerHost(mongoConfig.getConnectionsPerHost())
                .maxConnectionIdleTime(60000)
                .retryWrites(true)
                .readPreference(ReadPreference.secondary())
                .writeConcern(WriteConcern.MAJORITY)
                .build());
        this.mongoConfig = mongoConfig;
    }

    public void insert(String db, String coll, Document document) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        collection.insertOne(document);
    }

    public void insert(String db, String coll, List<Document> documents) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        collection.insertMany(documents);
    }

    public <T> Optional<T> findOne(String db, String coll, Bson filter, Class<T> clazz) {
        return findOne(db, coll, filter, null, clazz);
    }

    public <T> Optional<T> findOne(String db, String coll, Bson filter, Bson sort, Class<T> clazz) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        FindIterable<Document> findIterable = collection.find(filter);
        if (sort != null) {
            findIterable.sort(sort);
        }
        Document document = findIterable.first();
        if (document != null) {
            return Optional.of(ObjectMapperFactory.getMapper().convertValue(document, clazz));
        }
        return Optional.empty();
    }

    public long count(String db, String coll, Bson filter) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        return collection.countDocuments(filter);
    }

    public <T> List<T> find(String db, String coll, Bson filter, int limit, Bson sort, Bson projection, Class<T> clazz) {
        return find(db, coll, filter, limit, 0, sort, projection, clazz);
    }

    public <T> List<T> find(String db, String coll, Bson filter, int limit, int skip, Bson sort, Bson projection, Class<T> clazz) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        FindIterable<Document> findIterable = collection.find(filter);
        if (limit > 0) {
            findIterable = findIterable.limit(limit);
        }
        if (sort != null) {
            findIterable = findIterable.sort(sort).batchSize(limit);
        }
        if (projection != null) {
            findIterable = findIterable.projection(projection);
        }
        if (skip > 0) {
            findIterable.skip(skip);
        }
        MongoCursor<Document> documentCursor = findIterable.cursor();
        List<T> values = new ArrayList<>();
        while (documentCursor.hasNext()) {
            Document document = documentCursor.next();
            values.add(ObjectMapperFactory.getMapper().convertValue(document, clazz));
        }
        return values;
    }

    public long update(String db, String coll, Bson filter, Bson updatedDocument) {
        return update(db, coll, filter, updatedDocument, false);
    }

    public long update(String db, String coll, Bson filter, Bson updatedDocument, boolean upsert) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        UpdateResult updateResult = collection.updateOne(filter, updatedDocument, new UpdateOptions().upsert(upsert));
        return updateResult.getModifiedCount();
    }

    public long updateMany(String db, String coll, Bson filter, Bson updatedDocument, boolean upsert) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        UpdateResult updateResult = collection.updateMany(filter, updatedDocument, new UpdateOptions().upsert(upsert));
        return updateResult.getModifiedCount();
    }

    public <T> T findAndModify(String db, String coll, Bson filter, Bson updatedDocument, Class<T> clazz) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        Document updateResult = collection.findOneAndUpdate(filter, updatedDocument);
        return ObjectMapperFactory.getMapper().convertValue(updateResult, clazz);
    }

    public long replace(String db, String coll, Bson filter, Document updatedDocument) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        UpdateResult updateResult = collection.replaceOne(filter, updatedDocument, new ReplaceOptions().upsert(false));
        return updateResult.getModifiedCount();
    }
    public long delete(String db, String coll, Bson filter) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(coll);
        DeleteResult deleteResult = collection.deleteOne(filter);
        return deleteResult.getDeletedCount();
    }
}
