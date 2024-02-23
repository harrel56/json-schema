package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.net.URI;
import java.util.*;
import java.util.function.Supplier;

/**
 * Configurable factory for {@link Validator} class. Provides methods for ad-hoc validation, which parse schema each time they are invoked.
 *
 * @see Validator
 */
public final class ValidatorFactory {
    private Dialect dialect = new Dialects.Draft2020Dialect();
    private EvaluatorFactory evaluatorFactory;
    private Supplier<JsonNodeFactory> jsonNodeFactory = JacksonNode.Factory::new;
    private SchemaResolver schemaResolver = new DefaultSchemaResolver();
    private boolean disabledSchemaValidation = false;

    /**
     * Creates new instance of {@link Validator} using current configuration.
     *
     * @return new {@link Validator} instance
     */
    public Validator createValidator() {
        EvaluatorFactory compositeFactory = evaluatorFactory == null ? dialect.getEvaluatorFactory() : EvaluatorFactory.compose(evaluatorFactory, dialect.getEvaluatorFactory());
        JsonNodeFactory nodeFactory = jsonNodeFactory.get();
        return new Validator(dialect, compositeFactory, nodeFactory, schemaResolver, disabledSchemaValidation);
    }

    /**
     * Sets {@link Dialect}. Provided default is {@link Dialects.Draft2020Dialect}.
     *
     * @param dialect {@code Dialect} to be used
     * @return self
     */
    public ValidatorFactory withDialect(Dialect dialect) {
        this.dialect = Objects.requireNonNull(dialect);
        return this;
    }

    /**
     * Sets additional {@link EvaluatorFactory} to be used alongside with the core {@code EvaluatorFactory} provided by {@code Dialect}.
     * This is the best way to provide custom keyword support.
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
     * The default schema resolver resolves only official specification meta-schemas. Meta-schemas are loaded from classpath.
     *
     * @param schemaResolver {@code SchemaResolver} to be used
     * @return self
     * @see SpecificationVersion
     */
    public ValidatorFactory withSchemaResolver(SchemaResolver schemaResolver) {
        this.schemaResolver = SchemaResolver.compose(Objects.requireNonNull(schemaResolver), new DefaultSchemaResolver());
        return this;
    }

    /**
     * Disables schema validation against meta-schemas. This also disables vocabulary specific semantics.
     * <i>$schema</i> keyword will be ignored.
     *
     * @param disabledSchemaValidation if schema validation should be disabled
     * @return self
     */
    public ValidatorFactory withDisabledSchemaValidation(boolean disabledSchemaValidation) {
        this.disabledSchemaValidation = disabledSchemaValidation;
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

    static class DefaultSchemaResolver implements SchemaResolver {
        private final Map<URI, String> schemaCache = new HashMap<>();

        @Override
        public Result resolve(URI uri) {
            if (schemaCache.containsKey(uri)) {
                return Result.fromString(schemaCache.get(uri));
            }

            Optional<String> rawSchema = Arrays.stream(SpecificationVersion.values())
                    .map(spec -> spec.resolveResource(uri.toString()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            rawSchema.ifPresent(s -> schemaCache.put(uri, s));
            return rawSchema
                    .map(Result::fromString)
                    .orElse(Result.empty());
        }
    }
}
