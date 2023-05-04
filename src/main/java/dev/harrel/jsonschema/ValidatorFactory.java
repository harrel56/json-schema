package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

public final class ValidatorFactory {
    private static final String DEFAULT_META_SCHEMA = "https://json-schema.org/draft/2020-12/schema";

    private EvaluatorFactory evaluatorFactory = new CoreEvaluatorFactory();
    private JsonNodeFactory jsonNodeFactory = new JacksonNode.Factory();
    private SchemaResolver schemaResolver = new DefaultMetaSchemaResolver();
    private String defaultMetaSchemaUri = DEFAULT_META_SCHEMA;

    public boolean validate(String rawSchema, String rawInstance) {
        Validator validator = createValidator();
        URI uri = validator.registerSchema(rawSchema);
        return validator.validate(uri, rawInstance);
    }

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
