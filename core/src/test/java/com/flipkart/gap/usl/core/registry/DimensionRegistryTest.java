package com.flipkart.gap.usl.core.registry;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.dimension.DimensionSpec;
import com.flipkart.gap.usl.core.model.dimension.TestDimension;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.model.event.EventMapping;
import com.flipkart.gap.usl.core.model.external.ExternalEventSchema;
import com.flipkart.gap.usl.core.processor.BatchProcessor;
import com.flipkart.gap.usl.core.processor.event.MergeEventProcessor;
import com.flipkart.gap.usl.core.processor.event.UpdateEventProcessor;
import com.flipkart.gap.usl.core.store.event.EventMappingDBWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.when;

/**
 * Created by amarjeet.singh on 16/11/16.
 */
@PrepareForTest({DimensionRegistry.class, BatchProcessor.class})
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class DimensionRegistryTest {
    @Mock
    private EventMappingDBWrapper eventMappingDBWrapper;
    @InjectMocks
    private DimensionRegistry dimensionRegistry;
    @Mock
    private UpdateEventProcessor updateEventProcessor;
    @Mock
    private MergeEventProcessor mergeEventProcessor;
    @InjectMocks
    private BatchProcessor batchProcessor;

    private Map<String, Set<EventMapping>> eventMappings;
    private final String externalEventId = "productPageView";
    private final String internalPPVEventName = "testUpdateEvent";

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        eventMappings = new ConcurrentHashMap<>();
        Set<EventMapping> ppvEventMappings = new HashSet<>();
        ObjectNode mappings = ObjectMapperFactory.getMapper().createObjectNode();
        mappings.put("externalField", "internalField");
        mappings.put("/parent/test", "test");
        List<String> entityId = new ArrayList<>();
        entityId.add("/parent/accountId");
        EventMapping eventMapping = new EventMapping(externalEventId, internalPPVEventName, mappings, entityId, true);
        eventMapping.setValidations(ObjectMapperFactory.getMapper().readValue("{\"properties\": {\"parent\": {\"properties\": {\"test\": {\"type\": \"string\",\"pattern\": \"test123\"}},\"type\": \"object\",\"required\": [\"test\"]}},\"required\":[\"parent\"],\"type\": \"object\"}", JsonNode.class));
        ppvEventMappings.add(eventMapping);
        eventMappings.put(externalEventId, ppvEventMappings);
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry();
    }

    @Test
    public void testUpdateRegistry() throws Exception {
        when(eventMappingDBWrapper.getActiveEventMapping()).thenReturn(eventMappings);
        WhiteboxImpl.invokeMethod(dimensionRegistry, "updateRegistry");
        Map<String, Set<EventMapping>> registeredEvents = WhiteboxImpl.getInternalState(dimensionRegistry, "registeredEvents");
        Assert.assertEquals("Update Registry failed!", registeredEvents, eventMappings);
    }

    @Test
    public void testSetInternalEventMap() throws Exception {
        WhiteboxImpl.invokeMethod(dimensionRegistry, "setInternalEventMap", "com.flipkart.gap.usl.core.model.dimension");
        Map<String, Class<? extends DimensionEvent>> internalEventMap = WhiteboxImpl.getInternalState(dimensionRegistry, "internalEventMap");
        Assert.assertEquals("Update event not registered!", internalEventMap.get("testUpdateEvent"), TestDimension.TestDimensionUpdateEvent.class);
        Assert.assertEquals("Merge event not registered!", internalEventMap.get("testMergeEvent"), TestDimension.TestDimensionMergeEvent.class);
        Map<Class<? extends DimensionEvent>, Set<DimensionSpec>> registeredDimensions = WhiteboxImpl.getInternalState(dimensionRegistry, "registeredDimensions");
        Assert.assertTrue("Dimension not registered for Merge event!", registeredDimensions.get(TestDimension.TestDimensionMergeEvent.class).contains(new DimensionSpec(TestDimension.class, "test.dimension", Constants.DIMENSION_VERSION)));
        Assert.assertTrue("Dimension not registered for Update event!", registeredDimensions.get(TestDimension.TestDimensionUpdateEvent.class).contains(new DimensionSpec(TestDimension.class,"test.dimension", Constants.DIMENSION_VERSION)));
    }

    @Test()
    public void testGetDimensionEvents() throws Exception {
        testUpdateRegistry();
        testSetInternalEventMap();
        ExternalEventSchema externalEventSchema = new ExternalEventSchema();
        externalEventSchema.setName(externalEventId);
        ObjectNode externalEvent = ObjectMapperFactory.getMapper().createObjectNode();
        externalEvent.put("externalField", 12);
        ObjectNode parentNode = ObjectMapperFactory.getMapper().createObjectNode();
        parentNode.put("accountId", "ACC1234");
        parentNode.put("test","test123");
        externalEvent.set("parent", parentNode);
        externalEventSchema.setPayload(externalEvent);
        Set<DimensionEvent> registeredEvents = dimensionRegistry.getDimensionEvents(externalEventSchema);
        Assert.assertNotNull("Registered Events should not be null here!", registeredEvents);
        Assert.assertEquals("There should  be only 1 registered event", 1, registeredEvents.size());
        DimensionEvent dimensionEvent = registeredEvents.iterator().next();
        Assert.assertEquals("Event should be of class TestDimension.TestDimensionUpdateEvent", dimensionEvent.getClass(), TestDimension.TestDimensionUpdateEvent.class);
        Assert.assertEquals("Value of nested event property test should be test123", ((TestDimension.TestDimensionUpdateEvent)dimensionEvent).getTest(), "test123");
        TestDimension.TestDimensionUpdateEvent testDimensionUpdateEvent = (TestDimension.TestDimensionUpdateEvent) dimensionEvent;
        Assert.assertEquals("Value of copied field should be " + externalEvent.get("externalField"), externalEvent.get("externalField").asInt(), testDimensionUpdateEvent.getInternalField());
    }

    @Test()
    public void testGetDimensionEventsValidationFailure() throws Exception {
        testUpdateRegistry();
        testSetInternalEventMap();
        ExternalEventSchema externalEventSchema = new ExternalEventSchema();
        externalEventSchema.setName(externalEventId);
        ObjectNode externalEvent = ObjectMapperFactory.getMapper().createObjectNode();
        externalEvent.put("externalField", 12);
        ObjectNode parentNode = ObjectMapperFactory.getMapper().createObjectNode();
        parentNode.put("accountId", "ACC1234");
        externalEvent.set("parent", parentNode);
        externalEventSchema.setPayload(externalEvent);
        Set<DimensionEvent> registeredEvents = dimensionRegistry.getDimensionEvents(externalEventSchema);
        Assert.assertNotNull("Registered Events should not be null here!", registeredEvents);
        Assert.assertEquals("There should  be no registered event", 0, registeredEvents.size());
    }
}
