package com.flipkart.gap.usl.console.tests.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.gap.simple.proxy.CommandExecutable;
import com.flipkart.gap.simple.proxy.SimpleProxy;
import com.flipkart.gap.simple.proxy.request.MongoRequestWrapper;
import com.flipkart.gap.simple.proxy.response.CommandResponse;
import com.flipkart.gap.simple.proxy.response.MongoResponse;
import com.flipkart.gap.usl.console.resource.EventTypeResource;
import com.flipkart.gap.usl.core.config.MongoDBConfig;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.model.event.ExternalEvent;
import com.flipkart.gap.usl.core.model.event.Source;
import com.flipkart.gap.usl.core.store.event.EventTypeDBWrapper;
import com.flipkart.gap.usl.core.store.event.mongo.MongoEventTypeStore;
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
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by ankesh.maheshwari on 13/10/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SimpleProxy.class})
@PowerMockIgnore("javax.management.*")
public class EventTypeResourceTest {

    @Mock
    private CommandExecutable command;

    @InjectMocks
    private MongoEventTypeStore eventTypeStore;

    private SimpleProxy mockSimpleProxy;

    @Mock
    private MongoDBConfig mongoDBConfig;

    @InjectMocks
    private EventTypeResource eventTypeResource;

    @Mock
    private EventTypeDBWrapper eventTypeDBWrapper;

    @Mock
    private MongoResponse mongoResponse;

    @Before
    public void setUp() throws Exception {
        mockSimpleProxy = PowerMockito.mock(SimpleProxy.class);
        Whitebox.setInternalState(SimpleProxy.class,"PROXY",mockSimpleProxy);
        MockitoAnnotations.initMocks(this);
        eventTypeStore.setClientId("flipkart");
    }

    @Test
    public void createEventTest() throws IOException, AlreadyExistException, java.util.concurrent.ExecutionException, DataStoreException, IllegalAccessException {
        ObjectMapper mapper = ObjectMapperFactory.getMapper();
        Map<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("keY", "valUe");
        JsonNode actualObj = mapper.valueToTree(hashMap);
        ObjectNode node = ObjectMapperFactory.getMapper().createObjectNode();
        node.put("k112", "v112");
        ExternalEvent externalEvent = new ExternalEvent("test", actualObj, node, Source.FDP, "/a/b/c", true);
        Document eventDoc = ObjectMapperFactory.getMapper().convertValue(externalEvent, Document.class);

        MongoResponse mongoResponse = new MongoResponse(eventDoc);
        CommandResponse<MongoResponse> response = new CommandResponse<>(mongoResponse).withStatusCode(200);
        when(mockSimpleProxy.getMongoCommand(anyString(), any(HystrixCommandKey.class), any(MongoRequestWrapper.class))).thenReturn(command);
        when(command.exec()).thenReturn(response);
        Response r = eventTypeResource.createExternalEvent(externalEvent);
        Assert.assertEquals(r.getStatus(),SC_CREATED);

    }


    @Test
    public void updateEventTest() throws IOException, AlreadyExistException, RecordNotFoundException, java.util.concurrent.ExecutionException, IllegalAccessException, DataStoreException {
        ObjectMapper mapper = ObjectMapperFactory.getMapper();
        Map<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("keY", "valUe");
        JsonNode actualObj = mapper.valueToTree(hashMap);
        ObjectNode node = ObjectMapperFactory.getMapper().createObjectNode();
        hashMap.put("key", "values");
        ExternalEvent externalEvent = new ExternalEvent("test", actualObj, node, Source.FDP, "/a/b/d", true);

        Document eventDoc = ObjectMapperFactory.getMapper().convertValue(externalEvent, Document.class);

        MongoResponse mongoResponse = new MongoResponse(eventDoc);
        CommandResponse<MongoResponse> response = new CommandResponse<>(mongoResponse);
        when(mockSimpleProxy.getMongoCommand(anyString(), any(HystrixCommandKey.class), any(MongoRequestWrapper.class))).thenReturn(command);
        when(command.exec()).thenReturn(response);

        ExternalEvent retrivedEvent = eventTypeStore.get("test");
        Assert.assertEquals(retrivedEvent.getSource(),Source.FDP);
        Assert.assertEquals(retrivedEvent.getPivotPath(),"/a/b/d");
    }

    @Test
    public void removeEvent() throws RecordNotFoundException, AlreadyExistException, java.util.concurrent.ExecutionException, IOException, DataStoreException {
        ObjectMapper mapper = ObjectMapperFactory.getMapper();
        Map<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("keY", "valUe");
        JsonNode actualObj = mapper.valueToTree(hashMap);
        ObjectNode node = ObjectMapperFactory.getMapper().createObjectNode();
        hashMap.put("key", "values");
        ExternalEvent externalEvent = new ExternalEvent("test", actualObj, node, Source.FDP, "/a/b/c", true);

        Document eventDoc = ObjectMapperFactory.getMapper().convertValue(externalEvent, Document.class);

        MongoResponse mongoResponse = new MongoResponse(eventDoc);
        CommandResponse<MongoResponse> response = new CommandResponse<>(mongoResponse);
        when(mockSimpleProxy.getMongoCommand(anyString(), any(HystrixCommandKey.class), any(MongoRequestWrapper.class))).thenReturn(command);
        when(command.exec()).thenReturn(response);

        Response r = eventTypeResource.deleteExternalEvent("test");
        Assert.assertEquals(r.getStatus(),200);
    }

    @After
    public void tearDown() throws Exception {
    }

}



