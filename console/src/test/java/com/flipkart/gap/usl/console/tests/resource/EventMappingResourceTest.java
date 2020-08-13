package com.flipkart.gap.usl.console.tests.resource;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.gap.simple.proxy.CommandExecutable;
import com.flipkart.gap.simple.proxy.SimpleProxy;
import com.flipkart.gap.simple.proxy.request.MongoRequestWrapper;
import com.flipkart.gap.simple.proxy.response.CommandResponse;
import com.flipkart.gap.simple.proxy.response.MongoResponse;
import com.flipkart.gap.usl.app.dimension.DimensionEventType;
import com.flipkart.gap.usl.console.resource.EventMappingResource;
import com.flipkart.gap.usl.core.config.MongoDBConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.model.event.EventMapping;
import com.flipkart.gap.usl.core.store.event.EventMappingDBWrapper;
import com.flipkart.gap.usl.core.store.event.mongo.MongoEventMappingStore;
import com.flipkart.gap.usl.core.store.exception.AlreadyExistException;
import com.flipkart.gap.usl.core.store.exception.DataStoreException;
import com.flipkart.gap.usl.core.store.exception.RecordNotFoundException;
import com.netflix.hystrix.HystrixCommandKey;
import org.bson.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by ankesh.maheshwari on 13/10/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SimpleProxy.class})
@PowerMockIgnore("javax.management.*")
public class EventMappingResourceTest {

    @Mock
    private CommandExecutable command;

    @InjectMocks
    private MongoEventMappingStore mongoEventMappingStore;

    private SimpleProxy mockSimpleProxy;

    @Mock
    private MongoDBConfig mongoDBConfig;

    @InjectMocks
    private EventMappingResource eventMappingResource;

    @Mock
    private EventMappingDBWrapper eventTypeDBWrapper;

    @Mock
    private MongoResponse mongoResponse;

    @Before
    public void setUp() throws Exception {
        mockSimpleProxy = PowerMockito.mock(SimpleProxy.class);
        Whitebox.setInternalState(SimpleProxy.class,"PROXY",mockSimpleProxy);
        MockitoAnnotations.initMocks(this);
        mongoEventMappingStore.setClientId("flipkart");
    }

    @Test
    public void createEventMappingTest() throws IOException, AlreadyExistException, ExecutionException, RecordNotFoundException, DataStoreException {
        String eventId = "test";
        ObjectNode node = ObjectMapperFactory.getMapper().createObjectNode();
        node.put("k112", "v112");
        List<String> entityId = new ArrayList<>();
        entityId.add("testAccount");
        EventMapping eventMapping = new EventMapping(eventId, DimensionEventType.PPV, node, entityId,true);

        Document eventDoc = ObjectMapperFactory.getMapper().convertValue(eventMapping, Document.class);

        MongoResponse mongoResponse = new MongoResponse(eventDoc);
        CommandResponse<MongoResponse> response = new CommandResponse<>(mongoResponse).withStatusCode(200);
        when(mockSimpleProxy.getMongoCommand(anyString(), any(HystrixCommandKey.class), any(MongoRequestWrapper.class))).thenReturn(command);
        when(command.exec()).thenReturn(response);

        Response resp = eventMappingResource.createEventMapping(eventMapping);
        Assert.assertEquals(resp.getStatus(), SC_CREATED);
    }

    @Test
    public void getEventMappingTest() throws IOException, AlreadyExistException, ExecutionException, RecordNotFoundException, DataStoreException {
        String eventId = "test";
        ObjectNode node = ObjectMapperFactory.getMapper().createObjectNode();
        node.put("k112", "v112");
        List<String> entityId = new ArrayList<>();
        entityId.add("testAccount");
        EventMapping eventMapping = new EventMapping(eventId, DimensionEventType.PPV, node, entityId,true);

        Document eventDoc = ObjectMapperFactory.getMapper().convertValue(eventMapping, Document.class);

        MongoResponse mongoResponse = new MongoResponse(eventDoc);
        CommandResponse<MongoResponse> response = new CommandResponse<>(mongoResponse).withStatusCode(200);
        when(mockSimpleProxy.getMongoCommand(anyString(), any(HystrixCommandKey.class), any(MongoRequestWrapper.class))).thenReturn(command);
        when(command.exec()).thenReturn(response);

        Set<EventMapping> eventMappings = mongoEventMappingStore.getSourceEvents(eventId);
        Assert.assertEquals(eventMappings.size(), 1);
    }

    @Test
    public void disableEventMapping() throws ExecutionException, RecordNotFoundException, IOException, DataStoreException {
        String eventId = "test";
        ObjectNode node = ObjectMapperFactory.getMapper().createObjectNode();
        node.put("k112", "v112");
        List<String> entityId = new ArrayList<>();
        entityId.add("testAccount");
        EventMapping eventMapping = new EventMapping(eventId, DimensionEventType.PPV, node, entityId,true);

        Document eventDoc = ObjectMapperFactory.getMapper().convertValue(eventMapping, Document.class);

        MongoResponse mongoResponse = new MongoResponse(eventDoc);
        CommandResponse<MongoResponse> response = new CommandResponse<>(mongoResponse).withStatusCode(200);
        when(mockSimpleProxy.getMongoCommand(anyString(), any(HystrixCommandKey.class), any(MongoRequestWrapper.class))).thenReturn(command);
        when(command.exec()).thenReturn(response);

        Response resp = eventMappingResource.disableEventMapping(eventId, String.valueOf(DimensionEventType.PPV));
        Assert.assertEquals(resp.getStatus(), SC_OK);
    }

    @After
    public void tearDown() throws Exception {
    }
}



