package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Configurable factory for {@link Validator} class. Provides methods for ad-hoc validation, which parse schema each time they are invoked.
 *
 * @see Validator
 */
public final class ValidatorFactory {
    private Dialect dialect = new Dialects.Draft2020Dialect();
    private EvaluatorFactory evaluatorFactory;
    private Supplier<JsonNodeFactory> schemaNodeFactory = JacksonNode.Factory::new;
    private Supplier<JsonNodeFactory> instanceNodeFactory = schemaNodeFactory;
    private SchemaResolver schemaResolver = new DefaultSchemaResolver();
    private boolean disabledSchemaValidation = false;

    /**
     * Creates new instance of {@link Validator} using current configuration.
     *
     * @return new {@link Validator} instance
     */
    public Validator createValidator() {
        EvaluatorFactory compositeFactory = evaluatorFactory == null ? dialect.getEvaluatorFactory() : EvaluatorFactory.compose(evaluatorFactory, dialect.getEvaluatorFactory());
        JsonNodeFactory schemaFactory = schemaNodeFactory.get();
        JsonNodeFactory instanceFactory = instanceNodeFactory.get();
        return new Validator(dialect, compositeFactory, schemaFactory, instanceFactory, schemaResolver, disabledSchemaValidation);
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
     * Sets {@link JsonNodeFactory} to be used for both schema and data parsing.
     * If you require different factories for schemas and data, please use {@link ValidatorFactory#withJsonNodeFactories}
     * Provided default is {@link JacksonNode.Factory}.
     *
     * @param jsonNodeFactory {@code JsonNodeFactory} to be used
     * @return self
     */
    public ValidatorFactory withJsonNodeFactory(JsonNodeFactory jsonNodeFactory) {
        return withJsonNodeFactories(jsonNodeFactory, jsonNodeFactory);
    }

    /**
     * Sets one {@link JsonNodeFactory} for schema parsing and one for data parsing.
     * Might be useful when you expect schemas to be in a different format than data (JSON/YAML).
     * In most cases having two different factories is not required,
     * so please just use {@link ValidatorFactory#withJsonNodeFactory} whenever possible.
     * Provided default is {@link JacksonNode.Factory}.
     *
     * @param schemaNodeFactory   {@code JsonNodeFactory} to be used for parsing schemas
     * @param instanceNodeFactory {@code JsonNodeFactory} to be used for parsing data
     * @return self
     */
    public ValidatorFactory withJsonNodeFactories(JsonNodeFactory schemaNodeFactory, JsonNodeFactory instanceNodeFactory) {
        Objects.requireNonNull(schemaNodeFactory);
        Objects.requireNonNull(instanceNodeFactory);
        this.schemaNodeFactory = () -> schemaNodeFactory;
        this.instanceNodeFactory = () -> instanceNodeFactory;
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
        return validate(schemaNodeFactory.get().create(rawSchema), instanceNodeFactory.get().create(rawInstance));
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
        return validate(schemaNodeFactory.get().wrap(schemaProviderNode), instanceNodeFactory.get().create(rawInstance));
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
        return validate(schemaNode, instanceNodeFactory.get().create(rawInstance));
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
        return validate(schemaNodeFactory.get().create(rawSchema), instanceNodeFactory.get().wrap(instanceProviderNode));
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
        return validate(schemaNodeFactory.get().wrap(schemaProviderNode), instanceNodeFactory.get().wrap(instanceProviderNode));
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
        return validate(schemaNode, instanceNodeFactory.get().wrap(instanceProviderNode));
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
        return validate(schemaNodeFactory.get().create(rawSchema), instanceNode);
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
        return validate(schemaNodeFactory.get().wrap(schemaProviderNode), instanceNode);
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
        private final ConcurrentMap<String, String> schemaCache = new ConcurrentHashMap<>();

        @Override
        public Result resolve(String uri) {
            if (schemaCache.containsKey(uri)) {
                return Result.fromString(schemaCache.get(uri));
            }

            Optional<String> rawSchema = Arrays.stream(SpecificationVersion.values())
                    .map(spec -> spec.resolveResource(uri))
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
