package org.harrel.jsonschema;

public class MetaSchemaResolvingException extends JsonSchemaException {
    MetaSchemaResolvingException(String message, Throwable cause) {
        super(message, cause);
    }

    MetaSchemaResolvingException(String message) {
        super(message);
    }
}
