package com.flipkart.gap.usl.container.resource;

import lombok.Getter;
import org.apache.http.HttpStatus;

/**
 * Created by ashish.khatkar on 14/02/18.
 */
@Getter
public enum EventIngestorResponse {
    VALIDATION_FAILURE(HttpStatus.SC_BAD_REQUEST, "Validation failed for payload"),
    INGESTION_FAILURE(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Failed to ingest the event"),
    INTERNAL_SERVER_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Something bad happened"),
    INGESTION_SUCCESS(HttpStatus.SC_OK, "Event ingested successfully"),
    EVENT_MISSING(HttpStatus.SC_BAD_REQUEST, "Event type does not exist");

    private int statusCode;
    private String message;

    EventIngestorResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
