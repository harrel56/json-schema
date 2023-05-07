package dev.harrel.jsonschema;

/**
 * Exception type used to indicate meta-schema resolution failure.
 */
public class MetaSchemaResolvingException extends JsonSchemaException {
    MetaSchemaResolvingException(String message, Throwable cause) {
        super(message, cause);
    }

    MetaSchemaResolvingException(String message) {
        super(message);
    }
}
