package org.harrel.jsonschema;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SchemaParsingContext {
    private final URI baseUri;
    private final Map<String, Schema> schemaCache;
    private final Map<String, JsonNode> currentSchemaObject;

    private SchemaParsingContext(URI baseUri, Map<String, Schema> schemaCache, Map<String, JsonNode> currentSchemaObject) {
        this.baseUri = baseUri;
        this.schemaCache = schemaCache;
        this.currentSchemaObject = currentSchemaObject;
    }

    public SchemaParsingContext(URI baseUri) {
        this(baseUri, new HashMap<>(), Map.of());
    }

    public SchemaParsingContext withCurrentSchemaContext(Map<String, JsonNode> currentSchemaObject) {
        return new SchemaParsingContext(baseUri, schemaCache, Collections.unmodifiableMap(currentSchemaObject));
    }

    public Map<String, JsonNode> getCurrentSchemaObject() {
        return currentSchemaObject;
    }

    public String getAbsoluteUri(JsonNode node) {
        return getAbsoluteUri(node.getJsonPointer());
    }

    public String getAbsoluteUri(String jsonPointer) {
        if (jsonPointer.isEmpty()) {
            return baseUri.toString();
        } else if (jsonPointer.startsWith("#")) {
            return baseUri + jsonPointer;
        } else {
            return baseUri + "#" + jsonPointer;
        }
    }

    public void registerSchema(String uri, Schema schema) {
        if (schemaCache.containsKey(uri)) {
            throw new IllegalArgumentException("Duplicate schema registration, uri=" + uri);
        }
        schemaCache.put(uri, schema);
    }

    public boolean validateSchema(String uri, JsonNode node) {
        Schema schema = schemaCache.get(uri);
        if (!(schema instanceof IdentifiableSchema idSchema)) {
            throw new IllegalArgumentException(
                    "Couldn't find schema with uri=%s or it resolves to non-identifiable schema".formatted(uri));
        }
        ValidationContext ctx = new ValidationContext(idSchema, schemaCache);
        return idSchema.validate(ctx, node);
    }
}
