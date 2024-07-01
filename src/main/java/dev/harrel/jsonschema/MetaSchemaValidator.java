package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

class MetaValidationData {
    final Dialect dialect;
    final Set<String> activeVocabularies;

    MetaValidationData(Dialect dialect, Set<String> activeVocabularies) {
        this.dialect = dialect;
        this.activeVocabularies = activeVocabularies;
    }

    MetaValidationData(Dialect dialect) {
        this(dialect, dialect.getDefaultVocabularyObject().keySet());
    }
}

class MetaSchemaValidator {
    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;

    MetaSchemaValidator(JsonNodeFactory jsonNodeFactory, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
    }

    MetaValidationData processMetaSchema(JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node) {
        Objects.requireNonNull(metaSchemaUri);
        Schema schema = resolveMetaSchema(jsonParser, metaSchemaUri);
        validateSchema(schema, jsonParser, metaSchemaUri, schemaUri, node);
        return schema.getMetaValidationData();
    }

    void validateSchema(Schema schema, JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node) throws InvalidSchemaException {
        EvaluationContext ctx = new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver, false);
        if (!ctx.validateAgainstSchema(schema, node)) {
            throw new InvalidSchemaException(String.format("Schema [%s] failed to validate against meta-schema [%s]", schemaUri, metaSchemaUri),
                    new Validator.Result(false, ctx).getErrors());
        }
    }

    // todo actually perform vocabularies validation. Spec version need to have a field with requiredVocabs. IDK about supported vocabs
//        @Override
//        public MetaSchemaData determineActiveVocabularies(Map<String, Boolean> vocabulariesObject) {
//            List<String> missingRequiredVocabularies = dialect.getRequiredVocabularies().stream()
//                    .filter(vocab -> !vocabulariesObject.getOrDefault(vocab, false))
//                    .collect(Collectors.toList());
//            if (!missingRequiredVocabularies.isEmpty()) {
//                throw new VocabularyException(String.format("Required vocabularies [%s] were missing or marked optional in $vocabulary object", missingRequiredVocabularies));
//            }
//            List<String> unsupportedRequiredVocabularies = vocabulariesObject.entrySet().stream()
//                    .filter(Map.Entry::getValue)
//                    .map(Map.Entry::getKey)
//                    .filter(vocab -> !dialect.getSupportedVocabularies().contains(vocab))
//                    .collect(Collectors.toList());
//            if (!unsupportedRequiredVocabularies.isEmpty()) {
//                throw new VocabularyException(String.format("Following vocabularies [%s] are required but not supported", unsupportedRequiredVocabularies));
//            }
//            return vocabulariesObject.keySet();
//        }

    private Schema resolveMetaSchema(JsonParser jsonParser, URI uri) {
        CompoundUri compoundUri = CompoundUri.fromString(uri.toString());
        return OptionalUtil.firstPresent(
                () -> Optional.ofNullable(schemaRegistry.get(compoundUri)),
                () -> Optional.ofNullable(schemaRegistry.getDynamic(compoundUri))
        ).orElseGet(() -> resolveExternalSchema(jsonParser, uri));
    }

    private Schema resolveExternalSchema(JsonParser jsonParser, URI uri) {
        URI baseUri = UriUtil.getUriWithoutFragment(uri);
        if (schemaRegistry.get(baseUri) != null) {
            throw MetaSchemaResolvingException.resolvingFailure(uri.toString());
        }
        SchemaResolver.Result result = schemaResolver.resolve(baseUri.toString());
        if (result.isEmpty()) {
            throw MetaSchemaResolvingException.resolvingFailure(uri.toString());
        }
        try {
            result.toJsonNode(jsonNodeFactory).ifPresent(node -> jsonParser.parseRootSchema(baseUri, node));
        } catch (Exception e) {
            throw MetaSchemaResolvingException.parsingFailure(uri.toString(), e);
        }
        return resolveMetaSchema(jsonParser, uri);
    }
}

final class NoOpMetaSchemaValidator extends MetaSchemaValidator {
    public NoOpMetaSchemaValidator(JsonNodeFactory jsonNodeFactory, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        super(jsonNodeFactory, schemaRegistry, schemaResolver);
    }

    @Override
    void validateSchema(Schema schema, JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node) throws InvalidSchemaException {
        /* no validation */
    }
}
