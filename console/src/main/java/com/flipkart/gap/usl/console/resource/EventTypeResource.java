package com.flipkart.gap.usl.console.resource;

import com.flipkart.gap.usl.core.model.event.ExternalEvent;
import com.flipkart.gap.usl.core.store.event.EventTypeDBWrapper;
import com.flipkart.gap.usl.core.store.exception.AlreadyExistException;
import com.flipkart.gap.usl.core.store.exception.DataStoreException;
import com.flipkart.gap.usl.core.store.exception.RecordNotFoundException;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;


/**
 * Created by ankesh.maheshwari on 07/10/16.
 */
@Path("/event")
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
@Slf4j
public class EventTypeResource {
    @Inject
    private EventTypeDBWrapper eventTypeDBWrapper;

    @POST
    @Path("/create")
    public Response createExternalEvent(@Valid ExternalEvent externalEvent) {
        try {
            eventTypeDBWrapper.createEvent(externalEvent);
            return Response.status(HttpStatus.SC_CREATED).entity("CREATED").build();
        } catch (AlreadyExistException e) {
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity("Event Id already exist").build();
        } catch (Throwable t) {
            log.error("Error creating EventTypeStore {}", externalEvent.toString(), t);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @POST
    @Path("/update")
    public Response updateEventType(@Valid ExternalEvent externalEvent) {
        try {
            eventTypeDBWrapper.updateEvent(externalEvent);
            return Response.status(HttpStatus.SC_OK).entity("UPDATED").build();
        } catch (RecordNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND).entity("Event Id doesn't exist").build();
        } catch (Throwable t) {
            log.error("Error updating EventTypeStore {}", externalEvent.toString(), t);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @GET
    @Path("/{eventId}")
    public Response getExternalEvent(@PathParam("eventId") @NotBlank String eventId) {
        try {
            ExternalEvent externalEvent = eventTypeDBWrapper.getExternalEvent(eventId);
            if (externalEvent == null) {
                return Response.status(HttpStatus.SC_NOT_FOUND).entity("No data found for given Event Id").build();
            }
            return Response.status(HttpStatus.SC_OK).entity(externalEvent).build();
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Execution exception in get Event Type");
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @DELETE
    @Path("/delete/{sourceEventId}")
    public Response deleteExternalEvent(@NotBlank @PathParam("sourceEventId") final String sourceEventId) throws AlreadyExistException {
        try {
            eventTypeDBWrapper.removeEvent(sourceEventId);
            return Response.status(HttpStatus.SC_OK).entity("DELETED").build();
        } catch (RecordNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND).entity("Event Id doesn't exist").build();
        } catch (DataStoreException | IOException e) {
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Internal Server Error").build();
        }
    }

}

