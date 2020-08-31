package com.flipkart.gap.usl.core.model.event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * Created by ankesh.maheshwari on 13/10/16.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class EventMapping {
    // external event identifier
    @NotNull
    private String sourceEventId;
    // internal event Identifier
    @NotNull
    private String eventType;
    // external event to internal event
    @NotNull
    private JsonNode mapping;
    // optional validations ,send event only if these validations are passed.
    private JsonNode validations;

    @NotNull
    private List<String> entityIdPaths;
    @NotNull
    private boolean active = true;
    private long created;
    private long updated;

    public EventMapping(String sourceEventId, String eventType, JsonNode mapping, List<String> entityIdPaths, boolean active) {
        this.sourceEventId = sourceEventId;
        this.eventType = eventType;
        this.entityIdPaths = entityIdPaths;
        this.active = active;
        this.mapping = mapping;
    }
}
