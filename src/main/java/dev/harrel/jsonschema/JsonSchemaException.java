package dev.harrel.jsonschema;

/**
 * Base exception class for {@link dev.harrel.jsonschema}.
 */
public abstract class JsonSchemaException extends RuntimeException {
    JsonSchemaException(String message, Throwable cause) {
        super(message, cause);
    }

    JsonSchemaException(String message) {
        super(message);
    }

    JsonSchemaException() {}
}
