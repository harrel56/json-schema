package dev.harrel.jsonschema;

import java.net.URI;

/**
 * Exception type used to indicate meta-schema resolution failure.
 */
public class MetaSchemaResolvingException extends JsonSchemaException {
    private final URI uri;

    private MetaSchemaResolvingException(String message, Throwable cause, URI uri) {
        super(message, cause);
        this.uri = uri;
    }

    static MetaSchemaResolvingException resolvingFailure(URI uri) {
        return new MetaSchemaResolvingException(String.format("Cannot resolve meta-schema [%s]", uri), null, uri);
    }

    static MetaSchemaResolvingException parsingFailure(URI uri, Throwable cause) {
        return new MetaSchemaResolvingException(String.format("Parsing meta-schema [%s] failed", uri), cause, uri);
    }

    /**
     * Meta-schema uri getter.
     * @return URI for which the exception occurred
     */
    public URI getUri() {
        return uri;
    }
}
