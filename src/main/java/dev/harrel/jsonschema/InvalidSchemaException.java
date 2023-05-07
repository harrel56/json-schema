package dev.harrel.jsonschema;

/**
 * Exception type used to indicate that schema has failed validation against meta-schema.
 */
public class InvalidSchemaException extends JsonSchemaException {
    InvalidSchemaException(String message) {
        super(message);
    }
}
