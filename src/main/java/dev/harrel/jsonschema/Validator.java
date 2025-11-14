package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.net.URI;
import java.util.*;

/**
 * Main class for performing JSON schema validation. It can be created via {@link ValidatorFactory}.
 * Configuration of {@code Validator} is immutable. It uses <i>schema registry</i> to keep track of all the registered schemas,
 * which are registered either by {@link Validator#registerSchema(String)} method (and its overloads) or by {@link SchemaResolver} resolution.
 *
 * @see ValidatorFactory
 * @see Validator.Result
 */
public final class Validator {
    private final JsonNodeFactory schemaNodeFactory;
    private final JsonNodeFactory instanceNodeFactory;
    private final SchemaResolver schemaResolver;
    private final MessageProvider messageProvider;
    private final SchemaRegistry schemaRegistry;
    private final JsonParser jsonParser;

    Validator(JsonNodeFactory schemaNodeFactory,
              JsonNodeFactory instanceNodeFactory,
              SchemaResolver schemaResolver,
              MessageProvider messageProvider,
              SchemaRegistry schemaRegistry,
              JsonParser jsonParser) {
        this.schemaNodeFactory = Objects.requireNonNull(schemaNodeFactory);
        this.instanceNodeFactory = Objects.requireNonNull(instanceNodeFactory);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
        this.messageProvider = Objects.requireNonNull(messageProvider);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.jsonParser = Objects.requireNonNull(jsonParser);
    }

    /**
     * Registers schema and generates URI for it.
     *
     * @param rawSchema string representation of schema JSON
     * @return automatically generated URI for the registered schema <b>OR</b> value of <i>$id</i> keyword in <i>root</i> schema if present
     */
    public URI registerSchema(String rawSchema) {
        return registerSchema(schemaNodeFactory.create(rawSchema));
    }

    /**
     * Registers schema and generates URI for it.
     *
     * @param schemaProviderNode object representing schema JSON for currently set {@link JsonNodeFactory}.
     *                           E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @return automatically generated URI for the registered schema <b>OR</b> value of <i>$id</i> keyword in <i>root</i> schema if present
     */
    public URI registerSchema(Object schemaProviderNode) {
        return registerSchema(schemaNodeFactory.wrap(schemaProviderNode));
    }

    /**
     * Registers schema and generates URI for it.
     *
     * @param schemaNode {@link JsonNode} schema JSON, which could be created via {@link JsonNodeFactory}
     * @return automatically generated URI for the registered schema <b>OR</b> value of <i>$id</i> keyword in <i>root</i> schema if present
     */
    public URI registerSchema(JsonNode schemaNode) {
        return jsonParser.parseRootSchema(generateSchemaUri(), schemaNodeFactory.wrap(schemaNode));
    }

    /**
     * Registers schema at specified URI.
     *
     * @param uri       schema URI
     * @param rawSchema string representation of schema JSON
     * @return URI provided by user <b>OR</b> value of <i>$id</i> keyword in <i>root</i> schema if present
     */
    public URI registerSchema(URI uri, String rawSchema) {
        return registerSchema(uri, schemaNodeFactory.create(rawSchema));
    }

    /**
     * Registers schema at specified URI.
     *
     * @param uri                schema URI
     * @param schemaProviderNode object representing schema JSON for currently set {@link JsonNodeFactory}.
     *                           E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @return URI provided by user <b>OR</b> value of <i>$id</i> keyword in <i>root</i> schema if present
     */
    public URI registerSchema(URI uri, Object schemaProviderNode) {
        return registerSchema(uri, schemaNodeFactory.wrap(schemaProviderNode));
    }

    /**
     * Registers schema at specified URI.
     *
     * @param uri        schema URI
     * @param schemaNode {@link JsonNode} schema JSON, which could be created via {@link JsonNodeFactory}
     * @return URI provided by user <b>OR</b> value of <i>$id</i> keyword in <i>root</i> schema if present
     */
    public URI registerSchema(URI uri, JsonNode schemaNode) {
        return jsonParser.parseRootSchema(generateSchemaUri().resolve(uri), schemaNode);
    }

    /**
     * Validates instance JSON against schema resolved from provided URI.
     *
     * @param schemaUri   URI of schema to use for validation
     * @param rawInstance string representation of instance JSON
     * @return validation result
     */
    public Result validate(URI schemaUri, String rawInstance) {
        return validate(schemaUri, instanceNodeFactory.create(rawInstance));
    }

    /**
     * Validates instance JSON against schema resolved from provided URI.
     *
     * @param schemaUri            URI of schema to use for validation
     * @param instanceProviderNode object representing instance JSON for currently set {@link JsonNodeFactory}.
     *                             E.g. {@code com.fasterxml.jackson.databind.JsonNode} for default {@link JsonNodeFactory} ({@link JacksonNode.Factory})
     * @return validation result
     */
    public Result validate(URI schemaUri, Object instanceProviderNode) {
        return validate(schemaUri, instanceNodeFactory.wrap(instanceProviderNode));
    }

    /**
     * Validates instance JSON against a root schema resolved from provided URI.
     *
     * @param schemaUri    URI of a root schema to use for validation
     * @param instanceNode {@link JsonNode} instance JSON, which could be created via {@link JsonNodeFactory}
     * @return validation result
     */
    public Result validate(URI schemaUri, JsonNode instanceNode) {
        Schema schema = getRootSchema(schemaUri);
        EvaluationContext ctx = createNewEvaluationContext();
        boolean valid = ctx.validateAgainstRootSchema(schema, instanceNodeFactory.wrap(instanceNode));
        return new Result(valid, ctx);
    }

    private Schema getRootSchema(URI uri) {
        if (!uri.isAbsolute()) {
            uri = generateSchemaUri().resolve(uri);
        }
        CompoundUri compoundUri = CompoundUri.fromUri(uri);
        Schema schema = schemaRegistry.get(compoundUri);
        if (schema != null) {
            return schema;
        }
        schema = resolveExternalSchema(compoundUri);
        if (schema != null) {
            return schema;
        } else {
            throw new SchemaNotFoundException(compoundUri);
        }
    }

    private Schema resolveExternalSchema(CompoundUri compoundUri) {
        if (schemaRegistry.get(compoundUri.uri) != null) {
            return null;
        }
        return schemaResolver.resolve(compoundUri.uri.toString())
                .toJsonNode(schemaNodeFactory)
                .map(node -> {
                    jsonParser.parseRootSchema(compoundUri.uri, node);
                    return schemaRegistry.get(compoundUri);
                })
                .orElse(null);
    }

    private URI generateSchemaUri() {
        return URI.create("https://harrel.dev/" + UUID.randomUUID().toString().substring(0, 8));
    }

    private EvaluationContext createNewEvaluationContext() {
        return new EvaluationContext(schemaNodeFactory, jsonParser, schemaRegistry, schemaResolver, messageProvider);
    }

    /**
     * {@code Result} class represents validation outcome.
     */
    public static final class Result {
        private final boolean valid;
        private final List<Error> errors;
        private final List<Annotation> annotations;

        Result(boolean valid, EvaluationContext ctx) {
            this.valid = valid;
            this.errors = ctx.resolveErrors();
            this.annotations = ctx.getAnnotations();
        }

        /**
         * Checks if validation was successful.
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Returns collected annotation during schema validation.
         *
         * @return unmodifiable list of {@link Annotation}s
         */
        public List<Annotation> getAnnotations() {
            return annotations;
        }

        /**
         * Returns validation errors.
         *
         * @return unmodifiable list of {@link Error}s
         */
        public List<Error> getErrors() {
            return errors;
        }
    }
}
