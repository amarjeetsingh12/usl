package com.flipkart.gap.usl.core.store.event;

import com.flipkart.gap.usl.core.config.CacheConfig;
import com.flipkart.gap.usl.core.model.event.EventMapping;
import com.flipkart.gap.usl.core.store.exception.AlreadyExistException;
import com.flipkart.gap.usl.core.store.exception.DataStoreException;
import com.flipkart.gap.usl.core.store.exception.RecordNotFoundException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by ankesh.maheshwari on 13/10/16.
 */

@Singleton
public class EventMappingDBWrapper {

    private LoadingCache<String, Set<EventMapping>> eventMappingCacheMap;
    @Inject
    @Named("cacheConfig")
    private CacheConfig cacheConfig;
    @Inject
    private EventMappingStore eventMappingStore;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Inject
    public void init() {
        eventMappingCacheMap = CacheBuilder.newBuilder()
                .refreshAfterWrite(cacheConfig.getTtlInMinutes(), TimeUnit.MINUTES)
                .build(new CacheLoader<String, Set<EventMapping>>() {
                    @Override
                    public Set<EventMapping> load(String sourceEventId) throws Exception {
                        return eventMappingStore.getActiveSourceEvents(sourceEventId);
                    }

                    @Override
                    public ListenableFuture<Set<EventMapping>> reload(String sourceEventId, Set<EventMapping> oldValue) throws Exception {
                        ListenableFutureTask<Set<EventMapping>> task = ListenableFutureTask.create(() -> eventMappingStore.getActiveSourceEvents(sourceEventId));
                        executorService.execute(task);
                        return task;
                    }
                });
    }

    public void removeEventMapping(String eventId, String dimensionUpdateEvent) throws RecordNotFoundException, IOException, DataStoreException {
        eventMappingStore.removeMapping(eventId, dimensionUpdateEvent);
    }

    public void disableEventMapping(String eventId, String dimensionUpdateEvent) throws RecordNotFoundException, IOException, DataStoreException {
        eventMappingStore.disableMapping(eventId, dimensionUpdateEvent);
    }

    public Set<EventMapping> getSourceEventMapping(String eventId) throws RecordNotFoundException {
        return eventMappingStore.getSourceEvents(eventId);
    }

    public Set<EventMapping> getDimensionEventMapping(String eventId) throws RecordNotFoundException {
        return eventMappingStore.getDimensionUpdateEvents(eventId);
    }

    public Set<EventMapping> getActiveEventMapping(String eventId) throws RecordNotFoundException, ExecutionException {
        return this.eventMappingCacheMap.get(eventId);
    }

    public Map<String, Set<EventMapping>> getActiveEventMapping() throws ExecutionException {
        return eventMappingStore.getActiveEventMappings();
    }

    public void createEventMapping(EventMapping eventMapping) throws AlreadyExistException, IOException, DataStoreException {
        eventMappingStore.create(eventMapping);
    }


    public void updateEventMapping(EventMapping eventMapping, String sourceEvent, String dimensionUpdateEvent) throws AlreadyExistException, IOException, RecordNotFoundException, DataStoreException {
        eventMappingStore.update(eventMapping, sourceEvent, dimensionUpdateEvent);
    }
}
