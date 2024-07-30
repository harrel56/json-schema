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
    private final Map<URI, Dialect> dialects = new HashMap<>(Dialects.OFFICIAL_DIALECTS);
    private Dialect defaultDialect = new Dialects.Draft2020Dialect();
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
        Map<URI, Dialect> dialectsCopy = Collections.unmodifiableMap(new HashMap<>(dialects));
        JsonNodeFactory schemaFactory = schemaNodeFactory.get();
        JsonNodeFactory instanceFactory = instanceNodeFactory.get();
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        MetaSchemaValidator metaSchemaValidator = new MetaSchemaValidator(schemaFactory, schemaRegistry, schemaResolver);
        JsonParser jsonParser = new JsonParser(dialectsCopy, defaultDialect, evaluatorFactory, schemaRegistry, metaSchemaValidator, disabledSchemaValidation);
        return new Validator(schemaFactory, instanceFactory, schemaResolver, schemaRegistry, jsonParser);
    }

    /**
     * Registers a {@link Dialect} using {@link Dialect#getMetaSchema()} value.
     * If {@link Dialect#getMetaSchema()} returns null, the dialect will not be registered.
     * This method can be called multiple times to register multiple dialects.
     * Keep in mind that overriding an official dialect is also possible if you provide the same meta-schema.
     * Generally, this method should be used only when:
     * <ul>
     *     <li>You need your meta-schema to be recursive (same value in <code>$schema</code> and <code>$id</code>).</li>
     *     <li>You want to validate vocabularies integrity based on your dialect's required and supported vocabularies.</li>
     *     <li>You use <code>withDisabledSchemaValidation()</code> and want to define active vocabularies for your meta-schema.</li>
     * </ul>
     *
     * @param dialect {@code Dialect} to be registered
     * @return self
     */
    public ValidatorFactory withDialect(Dialect dialect) {
        // todo lets make meta-schema non-nullable at some point
        if (dialect.getMetaSchema() != null) {
            dialects.put(URI.create(dialect.getMetaSchema()), dialect);
        }
        return this;
    }

    /**
     * Sets default {@link Dialect} which will be used when {@code $schema} keyword is absent.
     * Provided default is {@link Dialects.Draft2020Dialect}.
     *
     * @param dialect {@code Dialect} to be used as default
     * @return self
     */
    public ValidatorFactory withDefaultDialect(Dialect dialect) {
        this.defaultDialect = Objects.requireNonNull(dialect);
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
     * Disables schema validation against meta-schemas.
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
