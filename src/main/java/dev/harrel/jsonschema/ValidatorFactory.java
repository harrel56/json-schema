package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.io.*;
import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Configurable factory for {@link Validator} class. Provides methods for ad-hoc validation, which parse schema each time they are invoked.
 *
 * @see Validator
 */
public final class ValidatorFactory {
    private static final String DEFAULT_META_SCHEMA = "https://json-schema.org/draft/2020-12/schema";

    private EvaluatorFactory evaluatorFactory = new Draft2020EvaluatorFactory();
    private Supplier<JsonNodeFactory> jsonNodeFactory = JacksonNode.Factory::new;
    private SchemaResolver schemaResolver = new DefaultMetaSchemaResolver();
    private String defaultMetaSchemaUri = DEFAULT_META_SCHEMA;
    private String test = ":)";

    /**
     * Creates new instance of {@link Validator} using current configuration.
     *
     * @return new {@link Validator} instance
     */
    public Validator createValidator() {
        return new Validator(evaluatorFactory, jsonNodeFactory.get(), schemaResolver, defaultMetaSchemaUri);
    }

    /**
     * Sets {@link EvaluatorFactory}. Provided default is {@link Draft2020EvaluatorFactory}.
     *
     * @param evaluatorFactory {@code EvaluatorFactory} to be used
     * @return self
     */
    public ValidatorFactory withEvaluatorFactory(EvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = Objects.requireNonNull(evaluatorFactory);
        return this;
    }

    /**
     * Sets {@link JsonNodeFactory}. Provided default is {@link JacksonNode.Factory}.
     *
     * @param jsonNodeFactory {@code JsonNodeFactory} to be used
     * @return self
     */
    public ValidatorFactory withJsonNodeFactory(JsonNodeFactory jsonNodeFactory) {
        Objects.requireNonNull(jsonNodeFactory);
        this.jsonNodeFactory = () -> jsonNodeFactory;
        return this;
    }

    /**
     * Composes {@link SchemaResolver} with default schema resolver.
     * The default schema resolver resolves only <a href="https://json-schema.org/draft/2020-12/schema">draft 2020-12</a> schema. It is loaded from classpath.
     *
     * @param schemaResolver {@code SchemaResolver} to be used
     * @return self
     */
    public ValidatorFactory withSchemaResolver(SchemaResolver schemaResolver) {
        this.schemaResolver = CompositeSchemaResolver.of(Objects.requireNonNull(schemaResolver), new DefaultMetaSchemaResolver());
        return this;
    }

    /**
     * Sets default meta-schema URI. Provided default is <i>https://json-schema.org/draft/2020-12/schema</i>.
     * Each schema that does not have <i>$schema</i> keyword will be validated against meta-schema resolved from this default URI.
     * Passing null will disable default validation against meta-schema, only schemas with <i>$schema</i> keyword will be validated then.
     *
     * @param defaultMetaSchemaUri default meta-schema URI or null
     * @return self
     */
    public ValidatorFactory withDefaultMetaSchemaUri(String defaultMetaSchemaUri) {
        this.defaultMetaSchemaUri = defaultMetaSchemaUri;
        return this;
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param rawSchema   string representation of schema JSON
     * @param rawInstance string representation of instance JSON
     * @return validation result
     */
    public Validator.Result validate(String rawSchema, String rawInstance) {
        return validate(jsonNodeFactory.get().create(rawSchema), jsonNodeFactory.get().create(rawInstance));
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param schemaProviderNode object representing schema JSON for currently set {@link JsonNodeFactory}.
     *                           E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @param rawInstance        string representation of instance JSON
     * @return validation result
     * @see ValidatorFactory#validate(String, String)
     */
    public Validator.Result validate(Object schemaProviderNode, String rawInstance) {
        return validate(jsonNodeFactory.get().wrap(schemaProviderNode), jsonNodeFactory.get().create(rawInstance));
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param schemaNode  {@link JsonNode} schema JSON, which could be created via {@link JsonNodeFactory}
     * @param rawInstance string representation of instance JSON
     * @return validation result
     * @see ValidatorFactory#validate(String, String)
     */
    public Validator.Result validate(JsonNode schemaNode, String rawInstance) {
        return validate(schemaNode, jsonNodeFactory.get().create(rawInstance));
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param rawSchema            string representation of schema JSON
     * @param instanceProviderNode object representing instance JSON for currently set {@link JsonNodeFactory}.
     *                             E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @return validation result
     * @see ValidatorFactory#validate(String, String)
     */
    public Validator.Result validate(String rawSchema, Object instanceProviderNode) {
        return validate(jsonNodeFactory.get().create(rawSchema), jsonNodeFactory.get().wrap(instanceProviderNode));
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param schemaProviderNode   object representing schema JSON for currently set {@link JsonNodeFactory}.
     *                             E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @param instanceProviderNode object representing instance JSON for currently set {@link JsonNodeFactory}.
     *                             E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @return validation result
     * @see ValidatorFactory#validate(String, String)
     */
    public Validator.Result validate(Object schemaProviderNode, Object instanceProviderNode) {
        return validate(jsonNodeFactory.get().wrap(schemaProviderNode), jsonNodeFactory.get().wrap(instanceProviderNode));
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param schemaNode           {@link JsonNode} schema JSON, which could be created via {@link JsonNodeFactory}
     * @param instanceProviderNode object representing instance JSON for currently set {@link JsonNodeFactory}.
     *                             E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @return validation result
     * @see ValidatorFactory#validate(String, String)
     */
    public Validator.Result validate(JsonNode schemaNode, Object instanceProviderNode) {
        return validate(schemaNode, jsonNodeFactory.get().wrap(instanceProviderNode));
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param rawSchema    string representation of schema JSON
     * @param instanceNode {@link JsonNode} instance JSON, which could be created via {@link JsonNodeFactory}
     * @return validation result
     * @see ValidatorFactory#validate(String, String)
     */
    public Validator.Result validate(String rawSchema, JsonNode instanceNode) {
        return validate(jsonNodeFactory.get().create(rawSchema), instanceNode);
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param schemaProviderNode object representing schema JSON for currently set {@link JsonNodeFactory}.
     *                           E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @param instanceNode       {@link JsonNode} instance JSON, which could be created via {@link JsonNodeFactory}
     * @return validation result
     * @see ValidatorFactory#validate(String, String)
     */
    public Validator.Result validate(Object schemaProviderNode, JsonNode instanceNode) {
        return validate(jsonNodeFactory.get().wrap(schemaProviderNode), instanceNode);
    }

    /**
     * Validates JSON <i>instance</i> against <i>schema</i>.
     * Each invocation creates temporary {@link Validator} which parses schema from scratch.
     * If you want to validate multiple JSON instances against same schema - please use {@link ValidatorFactory#createValidator()}
     * and use {@link Validator} class directly.
     *
     * @param schemaNode   {@link JsonNode} schema JSON, which could be created via {@link JsonNodeFactory}
     * @param instanceNode {@link JsonNode} instance JSON, which could be created via {@link JsonNodeFactory}
     * @return validation result
     * @see ValidatorFactory#validate(String, String)
     */
    public Validator.Result validate(JsonNode schemaNode, JsonNode instanceNode) {
        Validator validator = createValidator();
        URI uri = validator.registerSchema(schemaNode);
        return validator.validate(uri, instanceNode);
    }

    static class DefaultMetaSchemaResolver implements SchemaResolver {
        private String rawSchema;

        @Override
        public Result resolve(String uri) {
            if (DEFAULT_META_SCHEMA.equals(uri)) {
                if (rawSchema != null) {
                    return Result.fromString(rawSchema);
                }
                try (InputStream is = getClass().getResourceAsStream("/draft2020-12.json")) {
                    if (is != null) {
                        rawSchema = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining());
                        return Result.fromString(rawSchema);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return Result.empty();
        }
    }
}
