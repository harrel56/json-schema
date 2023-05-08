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
        return jsonParser.parseRootSchema(URI.create(UUID.randomUUID().toString()), schemaNode);
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
        boolean valid = schema.validate(ctx, instanceNode);
        return Result.fromEvaluationContext(valid, ctx);
    }

    private Schema getRootSchema(String uri) {
        Schema schema = schemaRegistry.get(uri);
        if (schema == null) {
            throw new IllegalArgumentException("Couldn't find schema with uri [%s]".formatted(uri));
        }
        return schema;
    }

    private EvaluationContext createNewEvaluationContext() {
        return new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver);
    }

    public static final class Result {
        private final boolean valid;
        private final List<Annotation> annotations;
        private final List<Annotation> validationAnnotations;

        Result(boolean valid, List<Annotation> annotations, List<Annotation> validationAnnotations) {
            this.valid = valid;
            this.annotations = Objects.requireNonNull(annotations);
            this.validationAnnotations = Objects.requireNonNull(validationAnnotations);
        }

        static Result fromEvaluationContext(boolean valid, EvaluationContext ctx) {
            return new Result(valid, List.copyOf(ctx.getAnnotations()), List.copyOf(ctx.getValidationAnnotations()));
        }

        public boolean isValid() {
            return valid;
        }

        public List<Annotation> getAnnotations() {
            return annotations;
        }

        public List<Annotation> getValidationAnnotations() {
            return validationAnnotations;
        }

        public List<Annotation> getErrors() {
            if (isValid()) {
                return List.of();
            } else {
                return validationAnnotations.stream()
                        .filter(a -> !a.successful())
                        .filter(a -> a.message() != null)
                        .toList();
            }
        }
    }
}
