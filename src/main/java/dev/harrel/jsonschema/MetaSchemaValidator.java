package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

final class MetaSchemaValidator {
    private static final String RESOLVING_ERROR_MSG = "Cannot resolve meta-schema [%s]";

    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;

    MetaSchemaValidator(JsonNodeFactory jsonNodeFactory, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
    }

    Set<String> validateSchema(JsonParser jsonParser, String metaSchemaUri, String schemaUri, JsonNode node) {
        Objects.requireNonNull(metaSchemaUri);
        Schema schema = resolveMetaSchema(jsonParser, metaSchemaUri);
        EvaluationContext ctx = new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver, schema.getActiveVocabularies());
        if (!ctx.validateAgainstSchema(schema, node)) {
            throw new InvalidSchemaException(String.format("Schema [%s] failed to validate against meta-schema [%s]", schemaUri, metaSchemaUri),
                    Validator.Result.fromEvaluationContext(false, ctx).getErrors());
        }
        return determineActiveVocabularies(schema.getVocabulariesObject());
    }

    Set<String> determineActiveVocabularies(Map<String, Boolean> vocabulariesObject) {
        Set<String> supportedVocabularies = Vocabulary.DEFAULT_VOCABULARIES_OBJECT.keySet(); // TODO as field
        List<String> unsupportedRequiredVocabularies = vocabulariesObject.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .filter(vocab -> !supportedVocabularies.contains(vocab))
                .collect(Collectors.toList());
        if (!unsupportedRequiredVocabularies.isEmpty()) {
            throw new IllegalArgumentException(String.format("Following vocabularies [%s] are required but not supported", unsupportedRequiredVocabularies)); // TODO new exception class?
        }
        return vocabulariesObject.keySet();
    }

    private Schema resolveMetaSchema(JsonParser jsonParser, String uri) {
        return OptionalUtil.firstPresent(
                () -> Optional.ofNullable(schemaRegistry.get(uri)),
                () -> Optional.ofNullable(schemaRegistry.getDynamic(uri))
        ).orElseGet(() -> resolveExternalSchema(jsonParser, uri));
    }

    private Schema resolveExternalSchema(JsonParser jsonParser, String uri) {
        String baseUri = UriUtil.getUriWithoutFragment(uri);
        if (schemaRegistry.get(baseUri) != null) {
            throw new MetaSchemaResolvingException(String.format(RESOLVING_ERROR_MSG, uri));
        }
        SchemaResolver.Result result = schemaResolver.resolve(baseUri);
        if (result.isEmpty()) {
            throw new MetaSchemaResolvingException(String.format(RESOLVING_ERROR_MSG, uri));
        }
        try {
            result.toJsonNode(jsonNodeFactory).ifPresent(node -> jsonParser.parseRootSchema(URI.create(baseUri), node));
        } catch (Exception e) {
            throw new MetaSchemaResolvingException(String.format("Parsing meta-schema [%s] failed", uri), e);
        }
        return resolveMetaSchema(jsonParser, uri);
    }
}
