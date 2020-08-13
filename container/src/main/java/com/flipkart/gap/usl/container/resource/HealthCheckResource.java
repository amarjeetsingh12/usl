package com.flipkart.gap.usl.container.resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/")
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
public class HealthCheckResource {
    private static boolean rotation = true;
    private String secretKey = "12390almlc1290";

    @GET
    @Path("/{status: status|elb-healthcheck}")
    public Response status() {
        if (rotation) {
            return Response.status(HttpStatus.SC_OK).entity("ok").header(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_NOT_FOUND).header(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN).build();
    }

    @POST
    @Path("/oor")
    public Response oor(@QueryParam("secret") @NotNull @NotEmpty String secret) {
        if (secret.equals(secretKey)) {
            rotation = false;
            return Response.status(HttpStatus.SC_OK).entity("oor").header(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN).build();
        } else {
            log.error("Unauthorised request to access this resource");
            return Response.status(HttpStatus.SC_NOT_FOUND).header(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN).build();
        }

    }

    @POST
    @Path("/bir")
    public Response bir(@QueryParam("secret") @NotNull @NotEmpty String secret) {
        if (secret.equals(secretKey)) {
            rotation = true;
            return Response.status(HttpStatus.SC_OK).entity("bir").header(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN).build();
        } else {
            log.error("Unauthorised request to access this resource");
            return Response.status(HttpStatus.SC_NOT_FOUND).header(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN).build();
        }

    }


}
