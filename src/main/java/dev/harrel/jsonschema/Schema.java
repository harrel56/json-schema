package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static dev.harrel.jsonschema.Evaluator.*;

final class Schema {
    private static final Evaluator TRUE_EVALUATOR = (ctx, node) -> Result.success();
    private static final Evaluator FALSE_EVALUATOR = (ctx, node) -> Result.failure("False schema always fails.");

    private final URI parentUri;
    private final String schemaLocation;
    private final List<EvaluatorWrapper> evaluators;
    private final Set<String> activeVocabularies;
    private final Map<String, Boolean> vocabulariesObject;

    Schema(URI parentUri,
           String schemaLocation,
           List<EvaluatorWrapper> evaluators,
           Set<String> activeVocabularies,
           Map<String, Boolean> vocabulariesObject) {
        this.parentUri = parentUri;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.activeVocabularies = Objects.requireNonNull(activeVocabularies);
        this.vocabulariesObject = Objects.requireNonNull(vocabulariesObject);
        List<EvaluatorWrapper> unsortedEvaluators = new ArrayList<>(Objects.requireNonNull(evaluators));
        unsortedEvaluators.sort(Comparator.comparingInt(Evaluator::getOrder));
        this.evaluators = Collections.unmodifiableList(unsortedEvaluators);
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

    List<EvaluatorWrapper> getEvaluators() {
        return evaluators;
    }

    Set<String> getActiveVocabularies() {
        return activeVocabularies;
    }

    Map<String, Boolean> getVocabulariesObject() {
        return vocabulariesObject;
    }
}
