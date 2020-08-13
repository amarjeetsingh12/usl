package com.flipkart.gap.usl.core.store.event;

import com.flipkart.gap.usl.core.model.event.ExternalEvent;
import com.flipkart.gap.usl.core.store.exception.AlreadyExistException;
import com.flipkart.gap.usl.core.store.exception.DataStoreException;
import com.flipkart.gap.usl.core.store.exception.RecordNotFoundException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ankesh.maheshwari on 10/10/16.
 */
public interface EventTypeStore {
    void create(ExternalEvent externalEvent) throws AlreadyExistException, IOException, DataStoreException;

    ExternalEvent get(String eventId) throws RecordNotFoundException, IOException;

    Map<String,ExternalEvent> getAll() throws RecordNotFoundException, IOException;

    void remove(String eventId) throws RecordNotFoundException, IOException, DataStoreException;

    void update(ExternalEvent externalEvent) throws RecordNotFoundException, InternalError, IOException, AlreadyExistException;

}
