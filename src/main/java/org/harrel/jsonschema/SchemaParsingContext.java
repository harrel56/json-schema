package org.harrel.jsonschema;

import java.net.URI;
import java.util.*;

public class SchemaParsingContext {
    private final String baseUri;
    private final URI parentUri;
    private final SchemaRegistry schemaRegistry;
    private final Map<String, JsonNode> currentSchemaObject;

    private SchemaParsingContext(String baseUri, URI parentUri, SchemaRegistry schemaRegistry, Map<String, JsonNode> currentSchemaObject) {
        this.baseUri = baseUri;
        this.parentUri = parentUri;
        this.schemaRegistry = schemaRegistry;
        this.currentSchemaObject = currentSchemaObject;
    }

    public SchemaParsingContext(String baseUri) {
        this(Objects.requireNonNull(baseUri), URI.create(baseUri), new SchemaRegistry(), Map.of());
    }

    public URI getParentUri() {
        return parentUri;
    }

    public SchemaParsingContext withParentUri(URI parentUri) {
        return new SchemaParsingContext(baseUri, parentUri, schemaRegistry, currentSchemaObject);
    }

    public SchemaParsingContext withCurrentSchemaContext(Map<String, JsonNode> currentSchemaObject) {
        return new SchemaParsingContext(baseUri, parentUri, schemaRegistry, Collections.unmodifiableMap(currentSchemaObject));
    }

    public Map<String, JsonNode> getCurrentSchemaObject() {
        return currentSchemaObject;
    }

    public String getAbsoluteUri(JsonNode node) {
        return getAbsoluteUri(node.getJsonPointer());
    }

    public String getAbsoluteUri(String jsonPointer) {
        if (jsonPointer.isEmpty()) {
            return baseUri + "#";
        } else if (jsonPointer.startsWith("#")) {
            return baseUri + jsonPointer;
        } else {
            return baseUri + "#" + jsonPointer;
        }
    }

    public void registerSchema(JsonNode schemaNode, List<ValidatorDelegate> validators) {
        schemaRegistry.registerSchema(this, schemaNode, validators);
    }

    public void registerIdentifiableSchema(URI uri, JsonNode schemaNode, List<ValidatorDelegate> validators) {
        schemaRegistry.registerIdentifiableSchema(this, uri, schemaNode, validators);
    }

    public boolean validateSchema(JsonNode node) {
        Map<String, Schema> schemaCache = schemaRegistry.asSchemaCache();
        Schema schema = schemaCache.get(baseUri);
        if (!(schema instanceof IdentifiableSchema idSchema)) {
            throw new IllegalArgumentException(
                    "Couldn't find schema with uri=%s or it resolves to non-identifiable schema".formatted(baseUri));
        }
        ValidationContext ctx = new ValidationContext(idSchema, schemaCache, new ArrayList<>());
        return idSchema.validate(ctx, node);
    }
}
