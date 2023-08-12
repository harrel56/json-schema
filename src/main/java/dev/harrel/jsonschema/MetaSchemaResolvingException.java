package dev.harrel.jsonschema;

/**
 * Exception type used to indicate meta-schema resolution failure.
 */
public class MetaSchemaResolvingException extends JsonSchemaException {
    private final String uri;

    private MetaSchemaResolvingException(String message, Throwable cause, String uri) {
        super(message, cause);
        this.uri = uri;
    }

    static MetaSchemaResolvingException resolvingFailure(String uri) {
        return new MetaSchemaResolvingException(String.format("Cannot resolve meta-schema [%s]", uri), null, uri);
    }

    static MetaSchemaResolvingException parsingFailure(String uri, Throwable cause) {
        return new MetaSchemaResolvingException(String.format("Parsing meta-schema [%s] failed", uri), cause, uri);
    }

    /**
     * Meta-schema uri getter.
     * @return uri string for which the exception occurred
     */
    public String getUri() {
        return uri;
    }
}
