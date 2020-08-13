package com.flipkart.gap.usl.core.store.event;

import com.flipkart.gap.usl.core.model.event.EventMapping;
import com.flipkart.gap.usl.core.store.exception.DataStoreException;
import com.flipkart.gap.usl.core.store.exception.RecordNotFoundException;

import java.util.Map;
import java.util.Set;

/**
 * Created by ankesh.maheshwari on 13/10/16.
 */
public interface EventMappingStore {
    Map<String, Set<EventMapping>> getActiveEventMappings();

    void create(EventMapping eventMapping) throws DataStoreException;

    Set<EventMapping> getSourceEvents(String eventId) throws RecordNotFoundException;

    Set<EventMapping> getActiveSourceEvents(String eventId) throws RecordNotFoundException;

    Set<EventMapping> getDimensionUpdateEvents(String eventId) throws RecordNotFoundException;

    void update(EventMapping eventMapping, String sourceEvent, String dimensionUpdateEvent) throws RecordNotFoundException, InternalError, DataStoreException;

    void disableMapping(String sourceEventId, String dimensionUpdateEvent) throws RecordNotFoundException, DataStoreException;

    void removeMapping(String sourceEventId, String dimensionUpdateEvent) throws RecordNotFoundException, DataStoreException;
}
