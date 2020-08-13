package com.flipkart.gap.usl.core.store.event;

import com.flipkart.gap.usl.core.config.CacheConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.model.event.ExternalEvent;
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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by ankesh.maheshwari on 10/10/16.
 */

@Singleton
@Slf4j
public class EventTypeDBWrapper {

    private LoadingCache<String, ExternalEvent> externalEventCache;
    @Inject
    @Named("cacheConfig")
    private CacheConfig cacheConfig;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    @Inject
    private EventTypeStore eventTypeStore;

    @Inject
    public void init() throws IOException, RecordNotFoundException {
        externalEventCache = CacheBuilder.newBuilder()
                .refreshAfterWrite(cacheConfig.getTtlInMinutes(), TimeUnit.MINUTES)
                .build(new CacheLoader<String, ExternalEvent>() {
                    @Override
                    public ExternalEvent load(String eventId) throws Exception {
                        return eventTypeStore.get(eventId);
                    }
                    @Override
                    public ListenableFuture<ExternalEvent> reload(String eventId, ExternalEvent oldValue) throws Exception {
                        ListenableFutureTask<ExternalEvent> task = ListenableFutureTask.create(() -> eventTypeStore.get(eventId));
                        executorService.execute(task);
                        return task;
                    }
                });
        externalEventCache.putAll(eventTypeStore.getAll());
        log.info("EventTypeDBWrapper created with events :{}", ObjectMapperFactory.getMapper().writeValueAsString(externalEventCache.asMap()));
    }

    public ExternalEvent getExternalEvent(String eventId) throws ExecutionException {
        return externalEventCache.get(eventId);
    }

    public void createEvent(ExternalEvent externalEvent) throws AlreadyExistException, IOException, DataStoreException {
        eventTypeStore.create(externalEvent);
    }

    public void updateEvent(ExternalEvent externalEvent) throws AlreadyExistException, IOException, RecordNotFoundException {
        eventTypeStore.update(externalEvent);
    }

    public void removeEvent(String eventId) throws AlreadyExistException, RecordNotFoundException, DataStoreException, IOException {
        eventTypeStore.remove(eventId);
    }

}
