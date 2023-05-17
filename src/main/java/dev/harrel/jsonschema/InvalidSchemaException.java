package dev.harrel.jsonschema;

import java.util.List;

/**
 * Exception type used to indicate that schema has failed validation against meta-schema.
 */
public class InvalidSchemaException extends JsonSchemaException {
    private final transient List<Error> errors;

    InvalidSchemaException(String message, List<Error> errors) {
        super(message);
        this.errors = errors;
    }

    /**
     * Errors getter.
     * @return list of validation errors
     */
    public List<Error> getErrors() {
        return errors;
    }
}
