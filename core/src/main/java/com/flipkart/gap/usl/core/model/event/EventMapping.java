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
    /*
        EntityId : First should be primary id and consecutive for the fallback, like if accountId is not there, update with deviceId.
        accepts path of the entityId like <"/core/visit/accountId","/core/visit/device/deviceId">
     */
    @NotNull
    private List<String> entityIdPaths;
    @NotNull
    private boolean active = true;

    public EventMapping(String sourceEventId, String eventType, JsonNode mapping, List<String> entityIdPaths, boolean active) {
        this.sourceEventId = sourceEventId;
        this.eventType = eventType;
        this.entityIdPaths = entityIdPaths;
        this.active = active;
        this.mapping = mapping;
    }
}
