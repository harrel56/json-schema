package org.harrel.jsonschema;

public class JsonSchemaException extends RuntimeException {
    JsonSchemaException(String message, Throwable cause) {
        super(message, cause);
    }

    JsonSchemaException(String message) {
        super(message);
    }
}
