package dev.harrel.jsonschema;

public enum SpecificationVersion {
    DRAFT2020_12("https://json-schema.org/draft/2020-12/schema", "/draft2020-12.json");
    private final String id;
    private final String resourcePath;

    SpecificationVersion(String id, String resourcePath) {
        this.id = id;
        this.resourcePath = resourcePath;
    }

    public String getId() {
        return id;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
