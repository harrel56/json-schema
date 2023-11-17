package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

interface MetaSchemaValidator {
    Set<String> validateSchema(JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node);

    Set<String> determineActiveVocabularies(Map<String, Boolean> vocabulariesObject);

    final class NoOpMetaSchemaValidator implements MetaSchemaValidator {
        private final Set<String> activeVocabularies;

        public NoOpMetaSchemaValidator(Set<String> activeVocabularies) {
            this.activeVocabularies = activeVocabularies;
        }

        @Override
        public Set<String> validateSchema(JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node) {
            return activeVocabularies;
        }

        @Override
        public Set<String> determineActiveVocabularies(Map<String, Boolean> vocabulariesObject) {
            return activeVocabularies;
        }
    }
    final class DefaultMetaSchemaValidator implements MetaSchemaValidator {
        private final Dialect dialect;
        private final JsonNodeFactory jsonNodeFactory;
        private final SchemaRegistry schemaRegistry;
        private final SchemaResolver schemaResolver;

        DefaultMetaSchemaValidator(Dialect dialect, JsonNodeFactory jsonNodeFactory, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
            this.dialect = Objects.requireNonNull(dialect);
            this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
            this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
            this.schemaResolver = Objects.requireNonNull(schemaResolver);
        }

        @Override
        public Set<String> validateSchema(JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node) {
            Objects.requireNonNull(metaSchemaUri);
            Schema schema = resolveMetaSchema(jsonParser, metaSchemaUri);
            EvaluationContext ctx = new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver, schema.getActiveVocabularies(), false);
            if (!ctx.validateAgainstSchema(schema, node)) {
                throw new InvalidSchemaException(String.format("Schema [%s] failed to validate against meta-schema [%s]", schemaUri, metaSchemaUri),
                        new Validator.Result(false, ctx).getErrors());
            }
            return determineActiveVocabularies(schema.getVocabulariesObject());
        }

        @Override
        public Set<String> determineActiveVocabularies(Map<String, Boolean> vocabulariesObject) {
            List<String> missingRequiredVocabularies = dialect.getRequiredVocabularies().stream()
                    .filter(vocab -> !vocabulariesObject.getOrDefault(vocab, false))
                    .collect(Collectors.toList());
            if (!missingRequiredVocabularies.isEmpty()) {
                throw new VocabularyException(String.format("Required vocabularies [%s] were missing or marked optional in $vocabulary object", missingRequiredVocabularies));
            }
            List<String> unsupportedRequiredVocabularies = vocabulariesObject.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .filter(vocab -> !dialect.getSupportedVocabularies().contains(vocab))
                    .collect(Collectors.toList());
            if (!unsupportedRequiredVocabularies.isEmpty()) {
                throw new VocabularyException(String.format("Following vocabularies [%s] are required but not supported", unsupportedRequiredVocabularies));
            }
            return vocabulariesObject.keySet();
        }

        private Schema resolveMetaSchema(JsonParser jsonParser, URI uri) {
            return OptionalUtil.firstPresent(
                    () -> Optional.ofNullable(schemaRegistry.get(uri)),
                    () -> Optional.ofNullable(schemaRegistry.getDynamic(uri))
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
}
