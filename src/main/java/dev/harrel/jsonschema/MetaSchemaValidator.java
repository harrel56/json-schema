package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

interface MetaSchemaValidator {
    Set<String> validateSchema(JsonParser jsonParser, String metaSchemaUri, String schemaUri, JsonNode node);

    Set<String> determineActiveVocabularies(Map<String, Boolean> vocabulariesObject);

    final class NoOpMetaSchemaValidator implements MetaSchemaValidator {
        private final Set<String> activeVocabularies;

        public NoOpMetaSchemaValidator(Set<String> activeVocabularies) {
            this.activeVocabularies = activeVocabularies;
        }

        @Override
        public Set<String> validateSchema(JsonParser jsonParser, String metaSchemaUri, String schemaUri, JsonNode node) {
            return activeVocabularies;
        }

        @Override
        public Set<String> determineActiveVocabularies(Map<String, Boolean> vocabulariesObject) {
            return activeVocabularies;
        }
    }
    final class DefaultMetaSchemaValidator implements MetaSchemaValidator {
        private static final String RESOLVING_ERROR_MSG = "Cannot resolve meta-schema [%s]";

        private final JsonNodeFactory jsonNodeFactory;
        private final SchemaRegistry schemaRegistry;
        private final SchemaResolver schemaResolver;
        private final Set<String> supportedVocabularies;

        DefaultMetaSchemaValidator(JsonNodeFactory jsonNodeFactory, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver, Set<String> supportedVocabularies) {
            this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
            this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
            this.schemaResolver = Objects.requireNonNull(schemaResolver);
            this.supportedVocabularies = supportedVocabularies;
        }

        @Override
        public Set<String> validateSchema(JsonParser jsonParser, String metaSchemaUri, String schemaUri, JsonNode node) {
            Objects.requireNonNull(metaSchemaUri);
            Schema schema = resolveMetaSchema(jsonParser, metaSchemaUri);
            EvaluationContext ctx = new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver, schema.getActiveVocabularies());
            if (!ctx.validateAgainstSchema(schema, node)) {
                throw new InvalidSchemaException(String.format("Schema [%s] failed to validate against meta-schema [%s]", schemaUri, metaSchemaUri),
                        Validator.Result.fromEvaluationContext(false, ctx).getErrors());
            }
            return determineActiveVocabularies(schema.getVocabulariesObject());
        }

        @Override
        public Set<String> determineActiveVocabularies(Map<String, Boolean> vocabulariesObject) {
            if (Boolean.FALSE.equals(vocabulariesObject.getOrDefault(Vocabulary.Draft2020.CORE, false))) {
                throw new VocabularyException(String.format("Vocabulary [%s] was missing or marked optional in $vocabulary object", Vocabulary.Draft2020.CORE));
            }
            List<String> unsupportedRequiredVocabularies = vocabulariesObject.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .filter(vocab -> !supportedVocabularies.contains(vocab))
                    .collect(Collectors.toList());
            if (!unsupportedRequiredVocabularies.isEmpty()) {
                throw new VocabularyException(String.format("Following vocabularies [%s] are required but not supported", unsupportedRequiredVocabularies));
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
}
