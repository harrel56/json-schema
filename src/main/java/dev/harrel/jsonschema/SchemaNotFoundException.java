package dev.harrel.jsonschema;

import java.net.URI;

/**
 * Exception type used to indicate that resolution of specific schema has failed.
 */
public class SchemaNotFoundException extends JsonSchemaException {
    private final String ref;

    SchemaNotFoundException(URI refUri, String refFragment) {
        this(refUri + "#" + refFragment);
    }

    SchemaNotFoundException(String ref) {
        super(String.format("Couldn't find schema with uri [%s]", ref));
        this.ref = ref;
    }

    /**
     * Schema ref getter.
     * @return reference string for which the resolution failed
     */
    public String getRef() {
        return ref;
    }
}
