package dev.harrel.jsonschema;

import java.net.URI;
import java.util.List;

/**
 * Exception type used to indicate that schema has failed validation against meta-schema.
 */
public class InvalidSchemaException extends JsonSchemaException {
    private final String schemaUriString;
    private final URI metaSchemaUri;
    private final transient List<Error> errors;

    InvalidSchemaException(String message, String schemaUriString, URI metaSchemaUri, List<Error> errors) {
        super(message);
        this.schemaUriString = schemaUriString;
        this.metaSchemaUri = metaSchemaUri;
        this.errors = errors;
    }

    /**
     * Schema URI string getter. This is explicitly not provided as a URI instance as it's not guaranteed to be
     * a valid URI - e.g. fragment (JSON pointer) can contain invalid characters like "^".
     * @return URI string of a schema which failed validation
     */
    public String getSchemaUriString() {
        return schemaUriString;
    }

    /**
     * Meta-schema URI getter.
     * @return URI of a schema against which the validation failed
     */
    public URI getMetaSchemaUri() {
        return metaSchemaUri;
    }

    /**
     * Errors getter.
     * @return list of validation errors
     */
    public List<Error> getErrors() {
        return errors;
    }
}
