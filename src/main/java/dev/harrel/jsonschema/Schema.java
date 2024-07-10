package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static dev.harrel.jsonschema.Evaluator.Result;

final class Schema {
    private static final Evaluator TRUE_EVALUATOR = (ctx, node) -> Result.success();
    private static final Evaluator FALSE_EVALUATOR = (ctx, node) -> Result.failure("False schema always fails");

    private final URI parentUri;
    private final String schemaLocation;
    private final String schemaLocationFragment;
    private final List<EvaluatorWrapper> evaluators;
    private final MetaSchemaData metaSchemaData;

    Schema(URI parentUri,
           String schemaLocation,
           List<EvaluatorWrapper> evaluators,
           MetaSchemaData metaSchemaData,
           Map<String, JsonNode> objectMap) {
        this.parentUri = Objects.requireNonNull(parentUri);
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.schemaLocationFragment = UriUtil.getJsonPointer(schemaLocation);
        this.evaluators = Collections.unmodifiableList(
                evaluators.stream()
                        .filter(evaluator -> evaluator.getVocabularies().isEmpty() ||
                                !Collections.disjoint(evaluator.getVocabularies(), metaSchemaData.activeVocabularies))
                        .sorted(Comparator.comparingInt(Evaluator::getOrder))
                        .collect(Collectors.toList())
        );

        Optional<Map<String, Boolean>> vocabulariesObject = JsonNodeUtil.getVocabulariesObject(objectMap);
        Set<String> vocabularies = vocabulariesObject
                .map(Map::keySet)
                .orElse(metaSchemaData.activeVocabularies);
        this.metaSchemaData = new MetaSchemaData(metaSchemaData.dialect,
                vocabulariesObject.orElse(null), vocabularies);
    }

    static Evaluator getBooleanEvaluator(boolean val) {
        return val ? TRUE_EVALUATOR : FALSE_EVALUATOR;
    }

    URI getParentUri() {
        return parentUri;
    }

    String getSchemaLocation() {
        return schemaLocation;
    }

    String getSchemaLocationFragment() {
        return schemaLocationFragment;
    }

    List<EvaluatorWrapper> getEvaluators() {
        return evaluators;
    }

    MetaSchemaData getMetaValidationData() {
        return metaSchemaData;
    }
}
