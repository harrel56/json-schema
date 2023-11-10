package dev.harrel.jsonschema;

/**
 * Exception type used to indicate that resolution of specific schema has failed.
 */
public class SchemaNotFoundException extends JsonSchemaException {
    private final String ref;

    SchemaNotFoundException(CompoundUri compoundUri) {
        super(String.format("Couldn't find schema with uri [%s]", compoundUri));
        this.ref = compoundUri.toString();
    }

    /**
     * Schema ref getter.
     * @return reference string for which the resolution failed
     */
    public String getRef() {
        return ref;
    }
}
