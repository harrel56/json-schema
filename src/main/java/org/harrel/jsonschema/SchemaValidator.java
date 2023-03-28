package org.harrel.jsonschema;

import org.harrel.jsonschema.providers.JacksonNode;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SchemaValidator {
    private static final ValidatorFactory DEFAULT_VALIDATOR_FACTORY = new CoreValidatorFactory();
    private static final JsonNodeFactory DEFAULT_JSON_NODE_FACTORY = new JacksonNode.Factory();
    private static final SchemaResolver DEFAULT_SCHEMA_RESOLVER = uri -> Optional.empty();

    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaResolver schemaResolver;
    private final SchemaRegistry schemaRegistry;
    private final JsonParser jsonParser;

    public static Builder builder() {
        return new Builder();
    }

    private SchemaValidator(ValidatorFactory validatorFactory, JsonNodeFactory jsonNodeFactory, SchemaResolver schemaResolver) {
        this.jsonNodeFactory = jsonNodeFactory;
        this.schemaResolver = schemaResolver;
        this.schemaRegistry = new SchemaRegistry();
        this.jsonParser = new JsonParser(this.jsonNodeFactory, validatorFactory, this.schemaRegistry);
    }

    public SchemaValidator() {
        this(DEFAULT_VALIDATOR_FACTORY, DEFAULT_JSON_NODE_FACTORY, DEFAULT_SCHEMA_RESOLVER);
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

    public static final class Builder {
        private ValidatorFactory validatorFactory = DEFAULT_VALIDATOR_FACTORY;
        private JsonNodeFactory jsonNodeFactory = DEFAULT_JSON_NODE_FACTORY;
        private SchemaResolver schemaResolver = DEFAULT_SCHEMA_RESOLVER;

        public Builder withValidatorFactory(ValidatorFactory validatorFactory) {
            this.validatorFactory = Objects.requireNonNull(validatorFactory);
            return this;
        }

        public Builder withJsonNodeFactory(JsonNodeFactory jsonNodeFactory) {
            this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
            return this;
        }

        public Builder withSchemaResolver(SchemaResolver schemaResolver) {
            this.schemaResolver = Objects.requireNonNull(schemaResolver);
            return this;
        }

        public SchemaValidator build() {
            return new SchemaValidator(validatorFactory, jsonNodeFactory, schemaResolver);
        }
    }
}
