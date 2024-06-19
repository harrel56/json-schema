package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static dev.harrel.jsonschema.Evaluator.*;

final class Schema {
    private static final Evaluator TRUE_EVALUATOR = (ctx, node) -> Result.success();
    private static final Evaluator FALSE_EVALUATOR = (ctx, node) -> Result.failure("False schema always fails");

    private final URI parentUri;
    private final String schemaLocation;
    private final String schemaLocationFragment;
    private final List<EvaluatorWrapper> evaluators;
    private final MetaValidationData metaValidationData; //todo rename

    Schema(URI parentUri,
           String schemaLocation,
           List<EvaluatorWrapper> evaluators,
           MetaValidationData metaValidationData) {
        this.parentUri = Objects.requireNonNull(parentUri);
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.schemaLocationFragment = UriUtil.getJsonPointer(schemaLocation);
        this.metaValidationData = Objects.requireNonNull(metaValidationData);
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

    String getSchemaLocationFragment() {
        return schemaLocationFragment;
    }

    List<EvaluatorWrapper> getEvaluators() {
        return evaluators;
    }

    MetaValidationData getMetaValidationData() {
        return metaValidationData;
    }
}
