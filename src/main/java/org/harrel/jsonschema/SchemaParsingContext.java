package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.Validator;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SchemaParsingContext {
    private final String baseUri;
    private final SchemaRegistry schemaRegistry;
    private final Map<String, JsonNode> currentSchemaObject;

    private SchemaParsingContext(String baseUri, SchemaRegistry schemaRegistry, Map<String, JsonNode> currentSchemaObject) {
        this.baseUri = baseUri;
        this.schemaRegistry = schemaRegistry;
        this.currentSchemaObject = currentSchemaObject;
    }

    public SchemaParsingContext(String baseUri) {
        this(Objects.requireNonNull(baseUri), new SchemaRegistry(), Map.of());
    }

    public String getBaseUri() {
        return baseUri;
    }

    public SchemaParsingContext withCurrentSchemaContext(Map<String, JsonNode> currentSchemaObject) {
        return new SchemaParsingContext(baseUri, schemaRegistry, Collections.unmodifiableMap(currentSchemaObject));
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

    public void registerSchema(JsonNode schemaNode, List<Validator> validators) {
        schemaRegistry.registerSchema(this, schemaNode, validators);
    }

    public void registerIdentifiableSchema(URI uri, JsonNode schemaNode, List<Validator> validators) {
        schemaRegistry.registerIdentifiableSchema(this, uri, schemaNode, validators);
    }

    public boolean validateSchema(String uri, JsonNode node) {
        Map<String, Schema> schemaCache = schemaRegistry.asSchemaCache();
        Schema schema = schemaCache.get(uri);
        if (!(schema instanceof IdentifiableSchema idSchema)) {
            throw new IllegalArgumentException(
                    "Couldn't find schema with uri=%s or it resolves to non-identifiable schema".formatted(uri));
        }
        ValidationContext ctx = new ValidationContext(idSchema, schemaCache);
        return idSchema.validate(ctx, node);
    }
}
