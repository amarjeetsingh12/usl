package com.flipkart.gap.usl.core.store.event.mongo;

import com.flipkart.gap.usl.core.config.MongoConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.model.event.EventMapping;
import com.flipkart.gap.usl.core.store.event.EventMappingStore;
import com.flipkart.gap.usl.core.store.exception.DataStoreException;
import com.flipkart.gap.usl.core.store.exception.RecordNotFoundException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

@Singleton
@Slf4j
public class MongoEventMappingStore implements EventMappingStore {
    @Inject
    @Named("event.mongoConfig")
    private MongoConfig mongoConfig;
    private MongoDAO mongoDAO;
    private static final String DB_NAME = "usl";
    private static final String MAPPING_COLLECTION_NAME = "mapping";
    private String dbName;

    @Inject
    public void init() {
        this.mongoDAO = new MongoDAO(mongoConfig);
        dbName = this.mongoConfig.getDbName();
    }

    @Override
    public Map<String, Set<EventMapping>> getActiveEventMappings() {
        Bson activeFilter = Filters.eq(MongoConstants.EVENT_MAPPING.ACTIVE, true);
        Bson sortOrder = Sorts.descending(MongoConstants.EVENT_MAPPING.UPDATED);
        List<EventMapping> eventMappings = mongoDAO.find(dbName, MAPPING_COLLECTION_NAME, activeFilter, -1, sortOrder, null, EventMapping.class);
        return eventMappings.stream()
                .collect(
                        Collectors.groupingBy(EventMapping::getSourceEventId, mapping(eventMapping -> eventMapping, toSet()))
                );
    }

    @Override
    public void create(EventMapping eventMapping) throws DataStoreException {
        mongoDAO.insert(dbName, MAPPING_COLLECTION_NAME, ObjectMapperFactory.getMapper().convertValue(eventMapping, Document.class));
    }

    @Override
    public Set<EventMapping> getSourceEvents(String eventId) throws RecordNotFoundException {
        Bson sourceEventFilter = Filters.eq(MongoConstants.EVENT_MAPPING.SOURCE_EVENT_ID, eventId);
        Bson sortOrder = Sorts.descending(MongoConstants.EVENT_MAPPING.UPDATED);
        List<EventMapping> eventMappings = mongoDAO.find(dbName, MAPPING_COLLECTION_NAME, sourceEventFilter, -1, sortOrder, null, EventMapping.class);
        return new HashSet<>(eventMappings);
    }

    @Override
    public Set<EventMapping> getActiveSourceEvents(String eventId) throws RecordNotFoundException {
        Bson sourceEventFilter = Filters.and(
                Filters.eq(MongoConstants.EVENT_MAPPING.SOURCE_EVENT_ID, eventId),
                Filters.eq(MongoConstants.EVENT_MAPPING.ACTIVE, true)
        );
        Bson sortOrder = Sorts.descending(MongoConstants.EVENT_MAPPING.UPDATED);
        List<EventMapping> eventMappings = mongoDAO.find(dbName, MAPPING_COLLECTION_NAME, sourceEventFilter, -1, sortOrder, null, EventMapping.class);
        return new HashSet<>(eventMappings);
    }

    @Override
    public Set<EventMapping> getDimensionUpdateEvents(String eventId) throws RecordNotFoundException {
        Bson sourceEventFilter = Filters.eq(MongoConstants.EVENT_MAPPING.DIMENSION_UPDATE_EVENT, eventId);
        Bson sortOrder = Sorts.descending(MongoConstants.EVENT_MAPPING.UPDATED);
        List<EventMapping> eventMappings = mongoDAO.find(dbName, MAPPING_COLLECTION_NAME, sourceEventFilter, -1, sortOrder, null, EventMapping.class);
        return new HashSet<>(eventMappings);
    }

    @Override
    public void update(EventMapping eventMapping, String sourceEvent, String dimensionUpdateEvent) throws RecordNotFoundException, InternalError, DataStoreException {
        Bson sourceEventFilter = Filters.and(
                Filters.eq(MongoConstants.EVENT_MAPPING.SOURCE_EVENT_ID, sourceEvent),
                Filters.eq(MongoConstants.EVENT_MAPPING.DIMENSION_UPDATE_EVENT, dimensionUpdateEvent)
        );
        Document eventDoc = ObjectMapperFactory.getMapper().convertValue(eventMapping, Document.class);
        mongoDAO.replace(dbName, MAPPING_COLLECTION_NAME, sourceEventFilter, eventDoc);
    }

    @Override
    public void disableMapping(String sourceEventId, String dimensionUpdateEvent) throws RecordNotFoundException, DataStoreException {
        Bson sourceEventFilter = Filters.and(
                Filters.eq(MongoConstants.EVENT_MAPPING.SOURCE_EVENT_ID, sourceEventId),
                Filters.eq(MongoConstants.EVENT_MAPPING.DIMENSION_UPDATE_EVENT, dimensionUpdateEvent)
        );
        mongoDAO.update(dbName, MAPPING_COLLECTION_NAME, sourceEventFilter, Updates.set(MongoConstants.EVENT_MAPPING.ACTIVE, false));
    }

    @Override
    public void removeMapping(String sourceEventId, String dimensionUpdateEvent) throws RecordNotFoundException, DataStoreException {
        Bson sourceEventFilter = Filters.and(
                Filters.eq(MongoConstants.EVENT_MAPPING.SOURCE_EVENT_ID, sourceEventId),
                Filters.eq(MongoConstants.EVENT_MAPPING.DIMENSION_UPDATE_EVENT, dimensionUpdateEvent)
        );
        mongoDAO.delete(dbName, MAPPING_COLLECTION_NAME, sourceEventFilter);
    }
}
