package org.harrel.jsonschema;

import java.net.URI;
import java.util.*;

public class SchemaParsingContext extends AbstractContext {
    private final URI parentUri;
    private final SchemaRegistry schemaRegistry;
    private final Map<String, JsonNode> currentSchemaObject;

    private SchemaParsingContext(URI baseUri, URI parentUri, SchemaRegistry schemaRegistry, Map<String, JsonNode> currentSchemaObject) {
        super(baseUri);
        this.parentUri = parentUri;
        this.schemaRegistry = schemaRegistry;
        this.currentSchemaObject = currentSchemaObject;
    }

    SchemaParsingContext(SchemaRegistry schemaRegistry, String baseUri) {
        this(URI.create(baseUri), URI.create(baseUri), schemaRegistry, Map.of());
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

    public void registerSchema(JsonNode schemaNode, List<ValidatorDelegate> validators) {
        schemaRegistry.registerSchema(this, schemaNode, validators);
    }

    public void registerIdentifiableSchema(URI uri, JsonNode schemaNode, List<ValidatorDelegate> validators) {
        schemaRegistry.registerIdentifiableSchema(this, uri, schemaNode, validators);
    }

    public boolean validateSchema(JsonParser jsonParser, SchemaResolver schemaResolver, JsonNode node) {
        Schema schema = schemaRegistry.get(baseUri.toString());
        if (!(schema instanceof IdentifiableSchema idSchema)) {
            throw new IllegalArgumentException(
                    "Couldn't find schema with uri=%s or it resolves to non-identifiable schema".formatted(baseUri));
        }
        ValidationContext ctx = new ValidationContext(baseUri, jsonParser, schemaRegistry, schemaResolver);
        return idSchema.validate(ctx, node);
    }
}
