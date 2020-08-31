package com.flipkart.gap.usl.container.resource;

import com.flipkart.gap.usl.core.model.event.EventMapping;
import com.flipkart.gap.usl.core.store.event.EventMappingDBWrapper;
import com.flipkart.gap.usl.core.store.exception.AlreadyExistException;
import com.flipkart.gap.usl.core.store.exception.DataStoreException;
import com.flipkart.gap.usl.core.store.exception.RecordNotFoundException;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpStatus;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;


/**
 * Created by ankesh.maheshwari on 07/10/16.
 */
@Path("/event-mapping")
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
@Slf4j
public class EventMappingResource {
    @Inject
    private EventMappingDBWrapper eventMappingDBWrapper;

    @POST
    @Path("/create")
    public Response createEventMapping(@Valid EventMapping eventMapping) {
        try {
            if (eventMapping.getMapping().findValues("entityId") == null) {
                log.error("Error creating Event Mapping {} Does not exist in mapping", eventMapping.getMapping());
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Event Mapping EntityId Does not exist").build();
            } else {
                eventMappingDBWrapper.createEventMapping(eventMapping);
                return Response.status(HttpStatus.SC_CREATED).entity("CREATED").build();
            }
        } catch (AlreadyExistException e) {
            log.error("Event Mapping Already exist, source event id {} , event type {}", eventMapping.getSourceEventId(), eventMapping.getEventType(), e);
            return Response.status(HttpStatus.SC_CONFLICT).entity("Event Id already exist").build();
        } catch (Throwable t) {
            log.error("Error creating Event Mapping {}", eventMapping.toString(), t);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Unable to create the Event Mapping, Internal Error").build();
        }
    }

    @POST
    @Path("/update/{sourceEvent}/{internalEventId}")
    public Response updateEventMapping(@Valid EventMapping eventMapping, @PathParam("sourceEvent") @NotBlank String sourceEvent, @PathParam("internalEventId") @NotBlank String internalEventId) {
        try {
            eventMappingDBWrapper.updateEventMapping(eventMapping, sourceEvent, internalEventId);
            return Response.status(HttpStatus.SC_OK).entity("UPDATED").build();
        } catch (RecordNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND).entity("Event Id doesn't exist").build();
        } catch (Throwable t) {
            log.error("Error updating Event Mapping {}", eventMapping.toString(), t);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @POST
    @Path("/disable/{sourceEvent}/{internalEventId}")
    public Response disableEventMapping(@PathParam("sourceEvent") @NotBlank String sourceEvent, @PathParam("internalEventId") @NotBlank String internalEventId) {
        try {
            eventMappingDBWrapper.disableEventMapping(sourceEvent, internalEventId);
            return Response.status(HttpStatus.SC_OK).entity("UPDATED").build();
        } catch (RecordNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND).entity("Event Id doesn't exist").build();
        } catch (Throwable t) {
            log.error("Error Disabling Event Mapping {}", sourceEvent + "_" + internalEventId, t);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @GET
    @Path("/sourceEvents/{sourceEventId}")
    public Response getEventMapping(@PathParam("sourceEventId") @NotBlank String sourceEventId) throws RecordNotFoundException {
        try {
            Set<EventMapping> eventMapping = eventMappingDBWrapper.getSourceEventMapping(sourceEventId);
            if (CollectionUtils.isEmpty(eventMapping)) {
                return Response.status(HttpStatus.SC_NOT_FOUND).entity("No data found for given Event Mapping Id").build();
            }
            return Response.status(HttpStatus.SC_OK).entity(eventMapping).build();
        } catch (RecordNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND).entity("Event Id doesn't exist").build();
        }
    }

    @GET
    @Path("/dimension/{dimensionUpdateEvent}")
    public Response getDimensionEventMapping(@PathParam("dimensionUpdateEvent") @NotBlank String dimensionUpdateEvent) throws RecordNotFoundException {
        Set<EventMapping> eventMapping = eventMappingDBWrapper.getDimensionEventMapping(dimensionUpdateEvent);
        if (CollectionUtils.isEmpty(eventMapping)) {
            return Response.status(HttpStatus.SC_NOT_FOUND).entity("No data found for given Event Mapping Id").build();
        }
        return Response.status(HttpStatus.SC_OK).entity(eventMapping).build();
    }

    @GET
    @Path("/events/active")
    public Response getAllEventMapping() {
        try {
            Map<String, Set<EventMapping>> eventMappingList = eventMappingDBWrapper.getActiveEventMapping();
            if (MapUtils.isEmpty(eventMappingList)) {
                return Response.status(HttpStatus.SC_NOT_FOUND).entity("No data found for active events").build();
            }
            return Response.status(HttpStatus.SC_OK).entity(eventMappingList).build();
        } catch (ExecutionException e) {
            log.error("Execution exception in getting all Event type Mapping");
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @DELETE
    @Path("/delete/{sourceEventId}/{dimensionUpdateEvent}")
    public Response deleteEventMapping(@NotBlank @PathParam("sourceEventId") final String sourceEventId, @NotBlank @PathParam("dimensionUpdateEvent") final String dimensionUpdateEvent) {
        try {
            eventMappingDBWrapper.removeEventMapping(sourceEventId, dimensionUpdateEvent);
            return Response.status(HttpStatus.SC_OK).entity("DELETED").build();
        } catch (DataStoreException e) {
            log.error("Execution exception in removing Event Mapping");
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        } catch (RecordNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND).entity("Event Id doesn't exist").build();
        } catch (IOException e) {
            log.error("Execution exception in removing Event Mapping ", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }
}

