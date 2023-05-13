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

    Schema(URI parentUri, String schemaLocation, List<EvaluatorWrapper> evaluators) {
        this.parentUri = parentUri;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        Objects.requireNonNull(evaluators);
        List<EvaluatorWrapper> unsortedEvaluators = new ArrayList<>(evaluators);
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
}
