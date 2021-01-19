package com.flipkart.gap.usl.core.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.gap.usl.core.constant.Constants;
import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.EntityDimensionCompositeKey;
import com.flipkart.gap.usl.core.model.InternalEventMeta;
import com.flipkart.gap.usl.core.model.dimension.*;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionEventType;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionMergeEvent;
import com.flipkart.gap.usl.core.model.dimension.event.DimensionUpdateEvent;
import com.flipkart.gap.usl.core.model.event.EventMapping;
import com.flipkart.gap.usl.core.model.external.ExternalEventSchema;
import com.flipkart.gap.usl.core.store.event.EventMappingDBWrapper;
import com.flipkart.gap.usl.core.store.exception.IngestionEventMappingException;
import com.flipkart.gap.usl.core.validator.SchemaValidator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import scala.Tuple2;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by amarjeet.singh on 04/10/16.
 */
@Slf4j
/**
 *  Class responsible for maintaining dimension registry, event mappings,
 */
@Singleton
public class DimensionRegistry {
    // sourceEventId to Active Mapping.
    private final Map<String, Set<EventMapping>> registeredEvents = new ConcurrentHashMap<>();
    //map storing internal event to dimension specs
    private final Map<Class<? extends DimensionEvent>, Set<DimensionSpec>> registeredDimensions = new ConcurrentHashMap<>();
    // map to store internal event name to internal event Class
    private final Map<String, Class<? extends DimensionEvent>> internalEventMap = new ConcurrentHashMap<>();
    // map to store dimensionName to Dimension Class
    private final Map<String, Class<? extends Dimension>> dimensionNameToClassMap = new ConcurrentHashMap<>();
    @Inject
    private EventMappingDBWrapper eventMappingDBWrapper;
    @Inject
    @Named("dimensionPackage")
    private String dimensionPackage;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Inject
    public void init() {
        updateRegistry();
        setInternalEventMap(dimensionPackage);
        schedule();
        log.info("DimensionRegistry created");
    }

    /**
     * Scheduler to update event mapping.
     */
    private void schedule() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateRegistry();
            } catch (Throwable throwable) {
                log.error("Error in update event registry scheduler ", throwable);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * @param externalEventSchema External Event to be mapped
     * @return Set of DimensionEvent as per registered Events.
     */
    public Set<DimensionEvent> getDimensionEvents(ExternalEventSchema externalEventSchema) {
        Set<EventMapping> eventMappings = registeredEvents.get(externalEventSchema.getName());
        Set<DimensionEvent> dimensionEvents = new HashSet<>();
        if (CollectionUtils.isNotEmpty(eventMappings)) {
            for (EventMapping eventMapping : eventMappings) {
                try {
                    if (eventMapping.getValidations() == null || SchemaValidator.getInstance().schemaApply(externalEventSchema.getPayload(), eventMapping.getValidations())) {
                        DimensionEvent dimensionEvent = this.parse(eventMapping, externalEventSchema.getPayload());
                        if (dimensionEvent != null) {
                            JmxReporterMetricRegistry.getInstance().histogram(dimensionEvent.getClass().getSimpleName(), System.currentTimeMillis() - dimensionEvent.getCreated());
                            dimensionEvents.add(dimensionEvent);
                        }
                    } else {
                        JmxReporterMetricRegistry.getInstance().markValidationFailureMeter(externalEventSchema.getName());
                        log.debug("Event mapping schema validation failed", eventMapping);
                    }
                } catch (Throwable e) {
                    JmxReporterMetricRegistry.getInstance().markEventMappingParsingFailure(eventMapping.getEventType(), eventMapping.getSourceEventId());
                    log.error("Error in Mapping for source event {}, to internal event {}", eventMapping.getSourceEventId(), eventMapping.getEventType(), e);
                }
            }
        } else {
            JmxReporterMetricRegistry.getInstance().markNoInternalEvent(externalEventSchema.getName());
            log.info("No internal event found for external event {}", externalEventSchema.getName());
        }
        return dimensionEvents;
    }

    public Class<? extends Dimension> getDimensionClass(String dimensionName) {
        return dimensionNameToClassMap.get(dimensionName);
    }

    public boolean containsDimensionClass(String dimensionName) {
        return dimensionNameToClassMap.containsKey(dimensionName);
    }

    /**
     * @param event Dimension event which needs to be converted.
     * @return Tuple2 of {@link EntityDimensionCompositeKey} and {@link InternalEventMeta} by finding all the Dimensions which are interested in this event.
     */
    public Stream<Tuple2<EntityDimensionCompositeKey, InternalEventMeta>> convertEventToInternalEventMeta(DimensionEvent event) {
        Set<DimensionSpec> dimensionSpecs = registeredDimensions.get(event.getClass());
        if (dimensionSpecs != null) {
            return dimensionSpecs.stream().map(dimensionSpec -> new Tuple2<>(new EntityDimensionCompositeKey(event.getEntityId(), dimensionSpec), new InternalEventMeta(event, dimensionSpec)));
        }
        return Stream.empty();
    }

    public Set<DimensionSpec> getDimensionSpecForEvent(Class<? extends DimensionEvent> eventType) {
        return registeredDimensions.get(eventType);
    }

    /**
     * @param dimensionPackage package from where to start scanning for Classes which are sub type of @see {@link Dimension}
     *                         uses {@link DimensionSpecs} to find each subclass's {@link DimensionMergeEvent}
     *                         and {@link DimensionUpdateEvent}
     */
    private void setInternalEventMap(String dimensionPackage) {
        Reflections reflections = new Reflections(dimensionPackage);
        try {
            Set<Class<?>> dimensionClasses = reflections.getTypesAnnotatedWith(DimensionSpecs.class);
            dimensionClasses.stream().forEach(annotatedClass -> {
                if (Dimension.class.isAssignableFrom(annotatedClass)) {
                    Class<? extends Dimension> dimensionClass = (Class<? extends Dimension>) annotatedClass;
                    DimensionSpecs dimensionSpecs = dimensionClass.getAnnotation(DimensionSpecs.class);
                    if (dimensionSpecs != null) {
                        if (dimensionSpecs.enabled()) {
                            Class<? extends DimensionUpdateEvent>[] dimensionUpdateEventClassArray = dimensionSpecs.updateEvents();
                            Class<? extends DimensionMergeEvent> dimensionMergeEventClass = dimensionSpecs.mergeEvent();
                            String dimensionName = dimensionSpecs.name();

                            // adding mapping for each update event class
                            for (Class<? extends DimensionUpdateEvent> dimensionUpdateEventClass : dimensionUpdateEventClassArray) {
                                if (!dimensionClass.isAssignableFrom(DummyUpdateEvent.class)) {
                                    addDimensionEventClassMapping(dimensionUpdateEventClass, dimensionClass, dimensionName, DimensionEventType.UPDATE);
                                }
                            }
                            // adding mapping for merge event class
                            if (!dimensionClass.isAssignableFrom(DummyMergeEvent.class)) {
                                addDimensionEventClassMapping(dimensionMergeEventClass, dimensionClass, dimensionName, DimensionEventType.MERGE);
                            }
                            dimensionNameToClassMap.put(dimensionName, dimensionClass);
                        }
                    } else {
                        log.error("Exception for event:{}.Reason Dimension does not have DimensionSpecs", dimensionClass);
                        System.exit(-1);
                    }
                } else {
                    log.error("DimensionSpecs annotated on non Dimension class");
                }
            });
            log.info("Dimension Event Map updated.");
        } catch (Throwable throwable) {
            log.error("Error in Dimensions registry. Every class having @DimensionType annotation must extend Dimension", throwable);
            System.exit(-1);
        }
    }

    /**
     * method to map dimensionEventClass to the dimension this event is for, and mapping eventName to dimensionEventClass
     */
    private void addDimensionEventClassMapping(Class<? extends DimensionEvent> dimensionEventClass, Class<? extends Dimension> dimensionClass, String dimensionName, DimensionEventType eventType) {
        Set<DimensionSpec> dimensionSpecSet = registeredDimensions.computeIfAbsent(dimensionEventClass, k -> new HashSet<>());
        dimensionSpecSet.add(new DimensionSpec(dimensionClass, dimensionName, Constants.DIMENSION_VERSION));
        EventSpecs eventSpecs = dimensionEventClass.getAnnotation(EventSpecs.class);
        internalEventMap.put(eventSpecs.name(), dimensionEventClass);
    }

    /**
     * method to update current active event mappings.
     */
    private synchronized void updateRegistry() {
        try {
            Map<String, Set<EventMapping>> newRegisteredEntries = eventMappingDBWrapper.getActiveEventMapping();
            if (MapUtils.isNotEmpty(newRegisteredEntries)) {
                //todo remove previous event.
                registeredEvents.putAll(newRegisteredEntries);
            }
            log.info("Registry updated");
        } catch (ExecutionException e) {
            log.error("Unable to fetch active campaigns", e);
        }
    }

    /**
     * @param eventMapping {@link EventMapping}
     * @param data         event data for current event
     * @return DimensionEvent by parsing External Event for internal Event.
     * @throws IngestionEventMappingException
     */
    private DimensionEvent parse(EventMapping eventMapping, ObjectNode data) throws IngestionEventMappingException {
        JmxReporterMetricRegistry.getInstance().markEventMappingParsingStarted(eventMapping.getSourceEventId(), eventMapping.getEventType());
        ObjectMapper mapper = ObjectMapperFactory.getMapper();
        ObjectNode dataNode = mapper.createObjectNode();
        Iterator<String> fieldNamesIterator = eventMapping.getMapping().fieldNames();
        while (fieldNamesIterator.hasNext()) {
            String fieldName = fieldNamesIterator.next();
            JsonNode fieldData;
            if (fieldName.startsWith("/")) {
                fieldData = data.at(fieldName);
            } else {
                fieldData = data.get(fieldName);
            }
            JsonNode derivedEventFieldNode = eventMapping.getMapping().get(fieldName);
            List<String> fieldsToUpdate = new LinkedList<>();
            if (derivedEventFieldNode.isArray()) {
                derivedEventFieldNode.forEach(arrayField -> fieldsToUpdate.add(arrayField.asText()));
            } else {
                fieldsToUpdate.add(derivedEventFieldNode.asText());
            }
            JsonNode finalFieldData = fieldData;
            fieldsToUpdate.forEach(s -> dataNode.set(s, finalFieldData));
        }

        /*
            EntityId should be kept separately, not in the mapping.
         */

        dataNode.set("entityId", getEntityId(eventMapping, data));

        Class<? extends DimensionEvent> derivedClass = this.internalEventMap.get(eventMapping.getEventType());
        if (DimensionEvent.class.isAssignableFrom(derivedClass)) {
            DimensionEvent dimensionEvent = mapper.convertValue(dataNode, derivedClass);
            if (StringUtils.isBlank(dimensionEvent.getEntityId())) {
                String dimensionEventJSON = "";
                try {
                    dimensionEventJSON = ObjectMapperFactory.getMapper().writeValueAsString(dimensionEvent);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                JmxReporterMetricRegistry.getInstance().markEntityIdMissing(eventMapping.getSourceEventId(), eventMapping.getEventType());
                throw new IngestionEventMappingException("EntityId is missing from Dimension Event {}" + dimensionEventJSON);
            }
            JmxReporterMetricRegistry.getInstance().markEventMappingParsingSuccess(eventMapping.getSourceEventId(), eventMapping.getEventType());
            return dimensionEvent;
        } else {
            JmxReporterMetricRegistry.getInstance().markNotDimensionEvent(eventMapping.getSourceEventId(), eventMapping.getEventType());
            throw new IngestionEventMappingException("Event should be of type " + DimensionEvent.class);
        }
    }

    private JsonNode getEntityId(EventMapping eventMapping, ObjectNode data) throws IngestionEventMappingException{
        if (eventMapping.getEntityIdPaths() != null) {
            return getEntityIdFromXPath(data, eventMapping.getEntityIdPaths());
        }
        Optional.ofNullable(eventMapping.getPivot()).orElseThrow(() ->
                new IngestionEventMappingException("EntityId is missing from Dimension Event {}" + data));
        return eventMapping.getPivot().getType().equals("xPath") ? getEntityIdFromXPath(data, eventMapping.getPivot().getValue()) :
                getEntityIdFromGroovy(eventMapping.getPivot().getExpression(), data);
    }

    private JsonNode getEntityIdFromGroovy(String script, ObjectNode data) {
        GroovyShell shell = new GroovyShell();
        shell.setProperty("data", data);
        String entityId = shell.evaluate(script).toString();
        ObjectMapper mapper = ObjectMapperFactory.getMapper();
        data.set("entityIdFromGroovy", mapper.convertValue(entityId, JsonNode.class));
        return data.at("/entityIdFromGroovy");
    }

    private JsonNode getEntityIdFromXPath(ObjectNode data, List<String> paths) {
        for (String path : paths) {
            JsonNode entityId = data.at(path);
            if (!entityId.isNull() && !entityId.toString().isEmpty()) {
                return entityId;
            }
        }
        return null;
    }
}
