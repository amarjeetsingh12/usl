package com.flipkart.gap.usl.core.store.event.mongo;

import com.flipkart.gap.usl.core.config.MongoConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.model.event.ExternalEvent;
import com.flipkart.gap.usl.core.store.event.EventTypeStore;
import com.flipkart.gap.usl.core.store.exception.AlreadyExistException;
import com.flipkart.gap.usl.core.store.exception.DataStoreException;
import com.flipkart.gap.usl.core.store.exception.RecordNotFoundException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class MongoEventStore implements EventTypeStore {
    @Inject
    @Named("event.mongoConfig")
    private MongoConfig mongoConfig;
    private MongoDAO mongoDAO;
    private static final String DB_NAME = "usl";
    private static final String EVENT_COLLECTION_NAME = "event";
    private String dbName;

    @Inject
    public void init() {
        this.mongoDAO = new MongoDAO(mongoConfig);
        dbName = mongoConfig.getDbName();
    }

    @Override
    public void create(ExternalEvent externalEvent) throws AlreadyExistException, IOException, DataStoreException {
        mongoDAO.insert(dbName, EVENT_COLLECTION_NAME, ObjectMapperFactory.getMapper().convertValue(externalEvent, Document.class));
    }

    @Override
    public ExternalEvent get(String eventId) throws RecordNotFoundException, IOException {
        Bson eventIdFilter = Filters.eq(MongoConstants.EVENT.EVENT_ID, eventId);
        Optional<ExternalEvent> externalEventOptional = mongoDAO.findOne(dbName, EVENT_COLLECTION_NAME, eventIdFilter, ExternalEvent.class);
        return externalEventOptional.orElse(null);
    }

    @Override
    public Map<String, ExternalEvent> getAll() throws RecordNotFoundException, IOException {
        Bson sortOrder = Sorts.descending(MongoConstants.EVENT_MAPPING.UPDATED);
        List<ExternalEvent> events = mongoDAO.find(dbName, EVENT_COLLECTION_NAME, new BsonDocument(), -1, sortOrder, null, ExternalEvent.class);
        return events.stream()
                .collect(Collectors.toMap(ExternalEvent::getEventId, event -> event));
    }

    @Override
    public void remove(String eventId) throws RecordNotFoundException, IOException, DataStoreException {
        Bson eventIdFilter = Filters.eq(MongoConstants.EVENT.EVENT_ID, eventId);
        mongoDAO.delete(dbName, EVENT_COLLECTION_NAME, eventIdFilter);
    }

    @Override
    public void update(ExternalEvent externalEvent) throws RecordNotFoundException, InternalError, IOException, AlreadyExistException {
        Bson eventIdFilter = Filters.eq(MongoConstants.EVENT.EVENT_ID, externalEvent.getEventId());
        Document eventDoc = ObjectMapperFactory.getMapper().convertValue(externalEvent, Document.class);
        mongoDAO.replace(dbName, EVENT_COLLECTION_NAME, eventIdFilter, eventDoc);
    }
}
