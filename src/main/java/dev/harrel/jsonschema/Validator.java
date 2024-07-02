package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

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
    private final SchemaRegistry schemaRegistry;
    private final JsonParser jsonParser;

    Validator(Map<URI, Dialect> dialects,
              Dialect defaultDialect,
              EvaluatorFactory evaluatorFactory,
              JsonNodeFactory schemaNodeFactory,
              JsonNodeFactory instanceNodeFactory,
              SchemaResolver schemaResolver,
              boolean disabledSchemaValidation) {
        this.schemaNodeFactory = Objects.requireNonNull(schemaNodeFactory);
        this.instanceNodeFactory = Objects.requireNonNull(instanceNodeFactory);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
        this.schemaRegistry = new SchemaRegistry();
        // todo move some creation up to ValidatorFactory
        MetaSchemaValidator metaSchemaValidator = new MetaSchemaValidator(this.schemaNodeFactory, schemaRegistry, schemaResolver);
        this.jsonParser = new JsonParser(dialects, defaultDialect, evaluatorFactory, schemaRegistry, metaSchemaValidator, disabledSchemaValidation);
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
        return jsonParser.parseRootSchema(generateSchemaUri(), schemaNode);
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
        return jsonParser.parseRootSchema(uri, schemaNode);
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
        boolean valid = ctx.validateAgainstSchema(schema, instanceNode);
        return new Result(valid, ctx);
    }

    private Schema getRootSchema(URI uri) {
        if (UriUtil.hasNonEmptyFragment(uri)) {
            throw new IllegalArgumentException(String.format("Root schema [%s] cannot contain non-empty fragments", uri));
        }
        return OptionalUtil.firstPresent(
                        () -> Optional.ofNullable(schemaRegistry.get(UriUtil.getUriWithoutFragment(uri))),
                        () -> resolveExternalSchema(uri)
                )
                .orElseThrow(() -> new SchemaNotFoundException(new CompoundUri(uri, "")));
    }

    private Optional<Schema> resolveExternalSchema(URI uri) {
        return schemaResolver.resolve(uri.toString())
                .toJsonNode(schemaNodeFactory)
                .map(node -> {
                    jsonParser.parseRootSchema(uri, node);
                    return schemaRegistry.get(uri);
                });
    }

    private URI generateSchemaUri() {
        return URI.create("https://harrel.dev/" + UUID.randomUUID().toString().substring(0, 8));
    }

    private EvaluationContext createNewEvaluationContext() {
        return new EvaluationContext(schemaNodeFactory, jsonParser, schemaRegistry, schemaResolver);
    }

    /**
     * {@code Result} class represents validation outcome.
     */
    public static final class Result {
        private final boolean valid;
        private final List<Error> errors;
        private final AnnotationTree annotationTree;
        private List<Annotation> annotations;

        Result(boolean valid, EvaluationContext ctx) {
            this.valid = valid;
            this.errors = unmodifiableList(ctx.getErrors().stream()
                    .filter(e -> e.getError() != null)
                    .collect(Collectors.toList()));
            this.annotationTree = ctx.getAnnotationTree();
        }

        /**
         * Checks if validation was successful.
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Returns collected annotation during schema validation.
         * @apiNote This getter works lazily. First call can be more expensive.
         *
         * @return unmodifiable list of {@link Annotation}s
         */
        public List<Annotation> getAnnotations() {
            if (annotations == null) {
                this.annotations = unmodifiableList(annotationTree.getAllAnnotations()
                        .stream()
                        .filter(a -> a.getAnnotation() != null)
                        .collect(Collectors.toList()));
            }
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
