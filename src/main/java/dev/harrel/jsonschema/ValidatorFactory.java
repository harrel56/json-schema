package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

public final class ValidatorFactory {
    private static final String DEFAULT_META_SCHEMA = "https://json-schema.org/draft/2020-12/schema";

    private EvaluatorFactory evaluatorFactory = new Draft2020EvaluatorFactory();
    private Supplier<JsonNodeFactory> jsonNodeFactory = JacksonNode.Factory::new;
    private SchemaResolver schemaResolver = new DefaultMetaSchemaResolver();
    private String defaultMetaSchemaUri = DEFAULT_META_SCHEMA;

    public Validator createValidator() {
        return new Validator(evaluatorFactory, jsonNodeFactory.get(), schemaResolver, defaultMetaSchemaUri);
    }

    public ValidatorFactory withEvaluatorFactory(EvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = Objects.requireNonNull(evaluatorFactory);
        return this;
    }

    public ValidatorFactory withJsonNodeFactory(JsonNodeFactory jsonNodeFactory) {
        Objects.requireNonNull(jsonNodeFactory);
        this.jsonNodeFactory = () -> jsonNodeFactory;
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

    public Validator.Result validate(String rawSchema, String rawInstance) {
        return validate(jsonNodeFactory.get().create(rawSchema), jsonNodeFactory.get().create(rawInstance));
    }

    public Validator.Result validate(Object schemaProviderNode, String rawInstance) {
        return validate(jsonNodeFactory.get().wrap(schemaProviderNode), jsonNodeFactory.get().create(rawInstance));
    }

    public Validator.Result validate(JsonNode schemaNode, String rawInstance) {
        return validate(schemaNode, jsonNodeFactory.get().create(rawInstance));
    }

    public Validator.Result validate(String rawSchema, Object instanceProviderNode) {
        return validate(jsonNodeFactory.get().create(rawSchema), jsonNodeFactory.get().wrap(instanceProviderNode));
    }

    public Validator.Result validate(Object schemaProviderNode, Object instanceProviderNode) {
        return validate(jsonNodeFactory.get().wrap(schemaProviderNode), jsonNodeFactory.get().wrap(instanceProviderNode));
    }

    public Validator.Result validate(JsonNode schemaNode, Object instanceProviderNode) {
        return validate(schemaNode, jsonNodeFactory.get().wrap(instanceProviderNode));
    }

    public Validator.Result validate(String rawSchema, JsonNode instanceNode) {
        return validate(jsonNodeFactory.get().create(rawSchema), instanceNode);
    }

    public Validator.Result validate(Object schemaProviderNode, JsonNode instanceNode) {
        return validate(jsonNodeFactory.get().wrap(schemaProviderNode), instanceNode);
    }

    public Validator.Result validate(JsonNode schemaNode, JsonNode instanceNode) {
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
