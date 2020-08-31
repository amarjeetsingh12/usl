package com.flipkart.gap.usl.core.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Created by ankesh.maheshwari on 07/10/16.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ExternalEvent {
    @NotNull
    private String eventId;
    @NotNull
    private JsonNode sampleData;
    @NotNull
    private JsonNode validations;
    @NotNull
    private String pivotPath;
    @NotNull
    private boolean active = true;
}
