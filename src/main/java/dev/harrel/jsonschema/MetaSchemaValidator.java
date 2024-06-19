package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

class MetaValidationData {
    final Set<String> activeVocabularies;
    final SpecificationVersion specificationVersion;

    public MetaValidationData(SpecificationVersion specificationVersion, Set<String> activeVocabularies) {
        this.specificationVersion = specificationVersion;
        this.activeVocabularies = activeVocabularies;
    }
}

interface MetaSchemaValidator {
    MetaValidationData validateSchema(JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node);

    final class NoOpMetaSchemaValidator implements MetaSchemaValidator {
        private final MetaValidationData metaValidationData;

        NoOpMetaSchemaValidator(MetaValidationData metaValidationData) {
            this.metaValidationData = metaValidationData;
        }

        @Override
        public MetaValidationData validateSchema(JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node) {
            return metaValidationData; //todo
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
        public MetaValidationData validateSchema(JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node) {
            Objects.requireNonNull(metaSchemaUri);
            Schema schema = resolveMetaSchema(jsonParser, metaSchemaUri);
            EvaluationContext ctx = new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver, false);
            if (!ctx.validateAgainstSchema(schema, node)) {
                throw new InvalidSchemaException(String.format("Schema [%s] failed to validate against meta-schema [%s]", schemaUri, metaSchemaUri),
                        new Validator.Result(false, ctx).getErrors());
            }
            return new MetaValidationData(schema.getSpecificationVersion(), schema.getVocabularies());
        }

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
