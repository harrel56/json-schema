package org.harrel.jsonschema;

import org.harrel.jsonschema.providers.JacksonNode;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class SchemaValidator {

    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaResolver schemaResolver;
    private final SchemaRegistry schemaRegistry;
    private final JsonParser jsonParser;

    public SchemaValidator() {
        this(new JacksonNode.Factory(), uri -> Optional.empty());
    }

    public SchemaValidator(JsonNodeFactory jsonNodeFactory, SchemaResolver schemaResolver) {
        this.jsonNodeFactory = jsonNodeFactory;
        this.schemaResolver = schemaResolver;
        this.schemaRegistry = new SchemaRegistry();
        this.jsonParser = new JsonParser(this.jsonNodeFactory, new CoreValidatorFactory(), this.schemaRegistry);
    }

    public URI registerSchema(String rawSchema) {
        return jsonParser.parseRootSchema(URI.create(UUID.randomUUID().toString()), jsonNodeFactory.create(rawSchema));
    }

    public URI registerSchema(Object schemaNode) {
        return jsonParser.parseRootSchema(URI.create(UUID.randomUUID().toString()), jsonNodeFactory.wrap(schemaNode));
    }

    public URI registerSchema(URI uri, String rawSchema) {
        return jsonParser.parseRootSchema(uri, jsonNodeFactory.create(rawSchema));
    }

    public URI registerSchema(URI uri, Object schemaNode) {
        return jsonParser.parseRootSchema(uri, jsonNodeFactory.wrap(schemaNode));
    }

    public boolean validate(URI schemaUri, String rawInstance) {
        return validate(schemaUri, jsonNodeFactory.create(rawInstance));
    }

    public boolean validate(URI schemaUri, Object instanceNode) {
        return validate(schemaUri, jsonNodeFactory.wrap(instanceNode));
    }

    public boolean validate(URI schemaUri, JsonNode instanceNode) {
        Schema schema = getRootSchema(schemaUri.toString());
        ValidationContext ctx = createNewValidationContext();
        return schema.validate(ctx, instanceNode);
    }

    private Schema getRootSchema(String uri) {
        Schema schema = schemaRegistry.get(uri);
        if (schema == null) {
            throw new IllegalStateException("Couldn't find schema with uri=%s or it resolves to non-identifiable schema".formatted(uri));
        }
        return schema;
    }

    private ValidationContext createNewValidationContext() {
        return new ValidationContext(jsonParser, schemaRegistry, schemaResolver);
    }
}
