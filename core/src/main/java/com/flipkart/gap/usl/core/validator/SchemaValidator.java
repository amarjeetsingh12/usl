package com.flipkart.gap.usl.core.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.github.fge.jsonschema.report.ProcessingReport;

import java.io.IOException;

/**
 * Created by ankesh.maheshwari on 18/10/16.
 */
public class SchemaValidator {
    private static SchemaValidator instance = new SchemaValidator();
    private static JsonValidator validator;


    public synchronized static SchemaValidator getInstance() {
        return instance;
    }

    private SchemaValidator() {
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        validator = factory.getValidator();
    }

    public boolean schemaApply(JsonNode input, JsonNode schema) throws IOException, ProcessingException {
        ProcessingReport output = validator.validate(schema, input);
        return output.isSuccess();
    }
}
