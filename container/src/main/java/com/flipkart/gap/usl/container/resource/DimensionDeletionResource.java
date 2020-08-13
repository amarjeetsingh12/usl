package com.flipkart.gap.usl.container.resource;

import com.codahale.metrics.annotation.Timed;
import com.flipkart.gap.usl.core.store.exception.DimensionDeleteException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/delete")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DimensionDeletionResource {

    DimensionDeletionService deletionService;

    static final String X_AUTH_USER = "X_AUTH_USER";
    static final String HEADER_VALUE = "usl-admin";

    @DELETE
    @Timed
    @Path("/entity/{entityId}/dimensionName/{dimensionName}")
    public Response getDimensionForEntity(@NotNull @PathParam("entityId") String entityId,
                                          @NotNull @PathParam("dimensionName") String dimensionName,
                                          @NotNull @HeaderParam(X_AUTH_USER) String authUser) {
        // Hack as this is supposed to be used by devs.
        if (!HEADER_VALUE.equals(authUser)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            deletionService.deleteEntity(entityId, dimensionName);
            return Response.ok().build();
        } catch (DimensionDeleteException e) {
            return Response.status(e.getCode()).entity(e.getMessage()).build();
        }
    }

}
