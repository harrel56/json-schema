package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Validator {
    private static final String DEFAULT_META_SCHEMA = "https://json-schema.org/draft/2020-12/schema";

    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaResolver schemaResolver;
    private final SchemaRegistry schemaRegistry;
    private final JsonParser jsonParser;

    public static Builder builder() {
        return new Builder();
    }

    Validator(EvaluatorFactory evaluatorFactory, JsonNodeFactory jsonNodeFactory, SchemaResolver schemaResolver, String defaultMetaSchemaUri) {
        evaluatorFactory = evaluatorFactory == null ? new CoreEvaluatorFactory() : evaluatorFactory;
        this.jsonNodeFactory = jsonNodeFactory == null ? new JacksonNode.Factory() : jsonNodeFactory;
        this.schemaResolver = decorateSchemaResolver(schemaResolver == null ? uri -> Optional.empty() : schemaResolver, defaultMetaSchemaUri);
        this.schemaRegistry = new SchemaRegistry();
        MetaSchemaValidator metaSchemaValidator = new MetaSchemaValidator(this.schemaRegistry, this.schemaResolver);
        this.jsonParser = new JsonParser(defaultMetaSchemaUri, this.jsonNodeFactory, evaluatorFactory, this.schemaRegistry, metaSchemaValidator);
    }

    public URI registerSchema(String rawSchema) {
        return registerSchema(jsonNodeFactory.create(rawSchema));
    }

    public URI registerSchema(Object schemaProviderNode) {
        return registerSchema(jsonNodeFactory.wrap(schemaProviderNode));
    }

    public URI registerSchema(JsonNode schemaNode) {
        return jsonParser.parseRootSchema(URI.create(UUID.randomUUID().toString()), schemaNode);
    }

    public URI registerSchema(URI uri, String rawSchema) {
        return registerSchema(uri, jsonNodeFactory.create(rawSchema));
    }

    public URI registerSchema(URI uri, Object schemaProviderNode) {
        return registerSchema(uri, jsonNodeFactory.wrap(schemaProviderNode));
    }

    public URI registerSchema(URI uri, JsonNode schemaNode) {
        return jsonParser.parseRootSchema(uri, schemaNode);
    }

    public boolean validate(URI schemaUri, String rawInstance) {
        return validate(schemaUri, jsonNodeFactory.create(rawInstance));
    }

    public boolean validate(URI schemaUri, Object instanceProviderNode) {
        return validate(schemaUri, jsonNodeFactory.wrap(instanceProviderNode));
    }

    public boolean validate(URI schemaUri, JsonNode instanceNode) {
        Schema schema = getRootSchema(schemaUri.toString());
        EvaluationContext ctx = createNewEvaluationContext();
        return schema.validate(ctx, instanceNode);
    }

    private Schema getRootSchema(String uri) {
        Schema schema = schemaRegistry.get(uri);
        if (schema == null) {
            throw new IllegalStateException("Couldn't find schema with uri [%s]".formatted(uri));
        }
        return schema;
    }

    private EvaluationContext createNewEvaluationContext() {
        return new EvaluationContext(jsonParser, schemaRegistry, schemaResolver);
    }

    private SchemaResolver decorateSchemaResolver(SchemaResolver schemaResolver, String defaultMetaSchemaUri) {
        if (DEFAULT_META_SCHEMA.equals(defaultMetaSchemaUri)) {
            DefaultMetaSchemaResolver defaultMetaSchemaResolver = new DefaultMetaSchemaResolver();
            return uri -> schemaResolver.resolve(uri).or(() -> defaultMetaSchemaResolver.resolve(uri));
        } else {
            return schemaResolver;
        }
    }

    // todo remove, replaced by ValidatorFactory
    public static final class Builder {
        private EvaluatorFactory evaluatorFactory;
        private JsonNodeFactory jsonNodeFactory;
        private SchemaResolver schemaResolver;
        private String defaultMetaSchemaUri = DEFAULT_META_SCHEMA;

        public Builder withEvaluatorFactory(EvaluatorFactory evaluatorFactory) {
            this.evaluatorFactory = Objects.requireNonNull(evaluatorFactory);
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

        public Validator build() {
            return new Validator(evaluatorFactory, jsonNodeFactory, schemaResolver, defaultMetaSchemaUri);
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
