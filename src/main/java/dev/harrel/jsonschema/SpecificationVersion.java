package dev.harrel.jsonschema;

/**
 * {@code SpecificationVersion} enum represents JSON Schema specification versions that are supported.
 */
public enum SpecificationVersion {
    DRAFT2020_12("https://json-schema.org/draft/2020-12/schema", "/draft2020-12.json");
    private final String id;
    private final String resourcePath;

    SpecificationVersion(String id, String resourcePath) {
        this.id = id;
        this.resourcePath = resourcePath;
    }

    /**
     * Returns ID which could be resolved to meta-schema.
     *
     * @return specification version ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns path to a classpath resource containing meta-schema.
     *
     * @return resource path
     */
    public String getResourcePath() {
        return resourcePath;
    }
}
