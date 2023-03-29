package org.harrel.jsonschema;

import org.harrel.jsonschema.providers.JacksonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SchemaValidator {
    private static final String DEFAULT_META_SCHEMA = "https://json-schema.org/draft/2020-12/schema";

    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaResolver schemaResolver;
    private final SchemaRegistry schemaRegistry;
    private final JsonParser jsonParser;

    public static Builder builder() {
        return new Builder();
    }

    private SchemaValidator(ValidatorFactory validatorFactory, JsonNodeFactory jsonNodeFactory, SchemaResolver schemaResolver, String defaultMetaSchemaUri) {
        validatorFactory = validatorFactory == null ? new CoreValidatorFactory() : validatorFactory;
        this.jsonNodeFactory = jsonNodeFactory == null ? new JacksonNode.Factory() : jsonNodeFactory;
        this.schemaResolver = decorateSchemaResolver(schemaResolver == null ? uri -> Optional.empty() : schemaResolver, defaultMetaSchemaUri);
        this.schemaRegistry = new SchemaRegistry();
        MetaSchemaValidator metaSchemaValidator = new MetaSchemaValidator(this.schemaRegistry, this.schemaResolver);
        this.jsonParser = new JsonParser(defaultMetaSchemaUri, this.jsonNodeFactory, validatorFactory, this.schemaRegistry, metaSchemaValidator);
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
            throw new IllegalStateException("Couldn't find schema with uri [%s]".formatted(uri));
        }
        return schema;
    }

    private ValidationContext createNewValidationContext() {
        return new ValidationContext(jsonParser, schemaRegistry, schemaResolver);
    }

    private SchemaResolver decorateSchemaResolver(SchemaResolver schemaResolver, String defaultMetaSchemaUri) {
        if (DEFAULT_META_SCHEMA.equals(defaultMetaSchemaUri)) {
            DefaultMetaSchemaResolver defaultMetaSchemaResolver = new DefaultMetaSchemaResolver();
            return uri -> schemaResolver.resolve(uri).or(() -> defaultMetaSchemaResolver.resolve(uri));
        } else {
            return schemaResolver;
        }
    }

    public static final class Builder {
        private ValidatorFactory validatorFactory;
        private JsonNodeFactory jsonNodeFactory;
        private SchemaResolver schemaResolver;
        private String defaultMetaSchemaUri = DEFAULT_META_SCHEMA;

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

        public Builder withDefaultMetaSchemaUri(String defaultMetaSchemaUri) {
            this.defaultMetaSchemaUri = defaultMetaSchemaUri;
            return this;
        }

        public SchemaValidator build() {
            return new SchemaValidator(validatorFactory, jsonNodeFactory, schemaResolver, defaultMetaSchemaUri);
        }
    }

    static class DefaultMetaSchemaResolver implements SchemaResolver {
        @Override
        public Optional<String> resolve(String uri) {
            if (DEFAULT_META_SCHEMA.equals(uri)) {
                try (InputStream is = getClass().getResourceAsStream("/draft2020-12.json")) {
                    if (is != null) {
                        return Optional.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return Optional.empty();
        }
    }
}
