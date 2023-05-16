package dev.harrel.jsonschema;

import java.util.List;

/**
 * Exception type used to indicate that schema has failed validation against meta-schema.
 */
public class InvalidSchemaException extends JsonSchemaException {
    private final transient List<EvaluationItem> errors;

    InvalidSchemaException(String message, List<EvaluationItem> errors) {
        super(message);
        this.errors = errors;
    }

    public List<EvaluationItem> getErrors() {
        return errors;
    }
}
