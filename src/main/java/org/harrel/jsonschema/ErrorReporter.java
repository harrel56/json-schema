package org.harrel.jsonschema;

public interface ErrorReporter {
    void reportError(ValidationContext ctx, JsonNode node);
}
