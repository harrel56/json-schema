package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ValidatorFactory {
    private static final String DEFAULT_META_SCHEMA = "https://json-schema.org/draft/2020-12/schema";

    private EvaluatorFactory evaluatorFactory = new CoreEvaluatorFactory();
    private JsonNodeFactory jsonNodeFactory = new JacksonNode.Factory();
    private SchemaResolver schemaResolver = new DefaultMetaSchemaResolver();
    private String defaultMetaSchemaUri = DEFAULT_META_SCHEMA;

    public Validator createValidator() {
        return new Validator(evaluatorFactory, jsonNodeFactory, schemaResolver, defaultMetaSchemaUri);
    }

    public ValidatorFactory withEvaluatorFactory(EvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = Objects.requireNonNull(evaluatorFactory);
        return this;
    }

    public ValidatorFactory withJsonNodeFactory(JsonNodeFactory jsonNodeFactory) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        return this;
    }

    public ValidatorFactory withSchemaResolver(SchemaResolver schemaResolver) {
        this.schemaResolver = CompositeSchemaResolver.of(Objects.requireNonNull(schemaResolver), new DefaultMetaSchemaResolver());
        return this;
    }

    public ValidatorFactory withDefaultMetaSchemaUri(String defaultMetaSchemaUri) {
        this.defaultMetaSchemaUri = defaultMetaSchemaUri;
        return this;
    }

    public ValidationResult validate(String rawSchema, String rawInstance) {
        return validate(jsonNodeFactory.create(rawSchema), jsonNodeFactory.create(rawInstance));
    }

    public ValidationResult validate(Object schemaProviderNode, String rawInstance) {
        return validate(jsonNodeFactory.wrap(schemaProviderNode), jsonNodeFactory.create(rawInstance));
    }

    public ValidationResult validate(JsonNode schemaNode, String rawInstance) {
        return validate(schemaNode, jsonNodeFactory.create(rawInstance));
    }

    public ValidationResult validate(String rawSchema, Object instanceProviderNode) {
        return validate(jsonNodeFactory.create(rawSchema), jsonNodeFactory.wrap(instanceProviderNode));
    }

    public ValidationResult validate(Object schemaProviderNode, Object instanceProviderNode) {
        return validate(jsonNodeFactory.wrap(schemaProviderNode), jsonNodeFactory.wrap(instanceProviderNode));
    }

    public ValidationResult validate(JsonNode schemaNode, Object instanceProviderNode) {
        return validate(schemaNode, jsonNodeFactory.wrap(instanceProviderNode));
    }

    public ValidationResult validate(String rawSchema, JsonNode instanceNode) {
        return validate(jsonNodeFactory.create(rawSchema), instanceNode);
    }

    public ValidationResult validate(Object schemaProviderNode, JsonNode instanceNode) {
        return validate(jsonNodeFactory.wrap(schemaProviderNode), instanceNode);
    }

    public ValidationResult validate(JsonNode schemaNode, JsonNode instanceNode) {
        Validator validator = createValidator();
        URI uri = validator.registerSchema(schemaNode);
        return validator.validate(uri, instanceNode);
    }

    static class DefaultMetaSchemaResolver implements SchemaResolver {
        @Override
        public Result resolve(String uri) {
            if (DEFAULT_META_SCHEMA.equals(uri)) {
                try (InputStream is = getClass().getResourceAsStream("/draft2020-12.json")) {
                    if (is != null) {
                        return Result.fromString(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return Result.empty();
        }
    }
}
