package dev.harrel.jsonschema;

/**
 * Exception type used to indicate that resolution of specific schema has failed.
 */
public class SchemaNotFoundException extends JsonSchemaException {
    private final String ref;

    SchemaNotFoundException(String ref) {
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
