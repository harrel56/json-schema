package dev.harrel.jsonschema;

public class InvalidSchemaException extends JsonSchemaException {
    InvalidSchemaException(String message) {
        super(message);
    }
}
