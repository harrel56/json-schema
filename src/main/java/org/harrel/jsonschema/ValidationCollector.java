package org.harrel.jsonschema;

public interface ValidationCollector<T> {
    void onSuccess(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode);
    void onFailure(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode, String errorMessage);
    T getOutput();
}
