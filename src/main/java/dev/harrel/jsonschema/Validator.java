package dev.harrel.jsonschema;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Validator {
    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaResolver schemaResolver;
    private final SchemaRegistry schemaRegistry;
    private final JsonParser jsonParser;

    Validator(EvaluatorFactory evaluatorFactory, JsonNodeFactory jsonNodeFactory, SchemaResolver schemaResolver, String defaultMetaSchemaUri) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
        this.schemaRegistry = new SchemaRegistry();
        MetaSchemaValidator metaSchemaValidator = new MetaSchemaValidator(this.jsonNodeFactory, this.schemaRegistry, this.schemaResolver);
        this.jsonParser = new JsonParser(defaultMetaSchemaUri, evaluatorFactory, this.schemaRegistry, metaSchemaValidator);
    }

    public URI registerSchema(String rawSchema) {
        return registerSchema(jsonNodeFactory.create(rawSchema));
    }

    public URI registerSchema(Object schemaProviderNode) {
        return registerSchema(jsonNodeFactory.wrap(schemaProviderNode));
    }

    public URI registerSchema(JsonNode schemaNode) {
        return jsonParser.parseRootSchema(generateSchemaUri(), schemaNode);
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

    public Result validate(URI schemaUri, String rawInstance) {
        return validate(schemaUri, jsonNodeFactory.create(rawInstance));
    }

    public Result validate(URI schemaUri, Object instanceProviderNode) {
        return validate(schemaUri, jsonNodeFactory.wrap(instanceProviderNode));
    }

    public Result validate(URI schemaUri, JsonNode instanceNode) {
        Schema schema = getRootSchema(schemaUri.toString());
        EvaluationContext ctx = createNewEvaluationContext();
        boolean valid = ctx.validateAgainstSchema(schema, instanceNode);
        return Result.fromEvaluationContext(valid, ctx);
    }

    private Schema getRootSchema(String uri) {
        Schema schema = schemaRegistry.get(uri);
        if (schema == null) {
            throw new IllegalArgumentException("Couldn't find schema with uri [%s]".formatted(uri));
        }
        return schema;
    }

    private URI generateSchemaUri() {
        return URI.create("https://harrel.dev/" + UUID.randomUUID().toString().substring(0, 8));
    }

    private EvaluationContext createNewEvaluationContext() {
        return new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver);
    }

    public static final class Result {
        private final boolean valid;
        private final List<EvaluationItem> evaluationItems;
        private final List<EvaluationItem> validationItems;

        Result(boolean valid, List<EvaluationItem> evaluationItems, List<EvaluationItem> validationItems) {
            this.valid = valid;
            this.evaluationItems = Objects.requireNonNull(evaluationItems);
            this.validationItems = Objects.requireNonNull(validationItems);
        }

        static Result fromEvaluationContext(boolean valid, EvaluationContext ctx) {
            return new Result(valid, List.copyOf(ctx.getEvaluationItems()), List.copyOf(ctx.getValidationItems()));
        }

        public boolean isValid() {
            return valid;
        }

        public List<EvaluationItem> getAnnotations() {
            return evaluationItems.stream()
                    .filter(a -> a.annotation() != null)
                    .toList();
        }

        public List<EvaluationItem> getErrors() {
            if (isValid()) {
                return List.of();
            } else {
                return validationItems.stream()
                        .filter(a -> !a.valid())
                        .filter(a -> a.error() != null)
                        .toList();
            }
        }
    }
}
