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
    private final SpecificationVersion specificationVersion;
    private final Set<String> vocabularies;

    Schema(URI parentUri,
           String schemaLocation,
           List<EvaluatorWrapper> evaluators,
           MetaValidationData metaValidationData,
           Map<String, JsonNode> objectMap) {
        this.parentUri = Objects.requireNonNull(parentUri);
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.schemaLocationFragment = UriUtil.getJsonPointer(schemaLocation);
        this.specificationVersion = metaValidationData.specificationVersion;
        this.evaluators = Collections.unmodifiableList(
                evaluators.stream()
                        .filter(evaluator -> evaluator.getVocabularies().isEmpty() ||
                                !Collections.disjoint(evaluator.getVocabularies(), metaValidationData.activeVocabularies))
                        .sorted(Comparator.comparingInt(Evaluator::getOrder))
                        .collect(Collectors.toList())
        );

        this.vocabularies = JsonNodeUtil.getVocabulariesObject(objectMap)
                .map(Map::keySet)
                .orElse(metaValidationData.activeVocabularies);
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

    public SpecificationVersion getSpecificationVersion() {
        return specificationVersion;
    }

    public Set<String> getVocabularies() {
        return vocabularies;
    }
}
