package com.flipkart.gap.usl.container.resource;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by ashish.khatkar on 05/02/18.
 */

@Slf4j
@Path("/usl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventIngestorResource {
    @Inject
    private EventIngestorService eventIngestorService;

    @POST
    @Timed
    @Path("/eventIngestor/{eventName}")
    public Response ingestEvent(@NotNull @PathParam("eventName") String eventName,
                                @NotNull ObjectNode payload) {
        EventIngestorResponse response = eventIngestorService.ingest(payload, eventName, IngestionType.Async);
        return Response.status(response.getStatusCode()).entity(response.getMessage()).build();
    }

    /**
     * ingestor API to insert data in sync
     *
     * @param eventName
     * @param payload
     * @return
     */
    @POST
    @Timed
    @Path("/eventIngestor/sync/{eventName}")
    public Response ingestEventSync(@NotNull @PathParam("eventName") String eventName,
                                @NotNull ObjectNode payload) {
        EventIngestorResponse response = eventIngestorService.ingest(payload, eventName, IngestionType.Sync);
        return Response.status(response.getStatusCode()).entity(response.getMessage()).build();
    }
}
