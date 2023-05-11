package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static dev.harrel.jsonschema.Evaluator.*;

/**
 * {@code Schema} class is a representation of a JSON schema (subschema as well).
 * It consists of list of {@link Evaluator}s to be executed for this schema.
 */
public final class Schema {
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

    /**
     * Performs validation and mutates {@link EvaluationContext} accordingly.
     * @param ctx evaluation context
     * @param node current instance node
     * @return {@code true} if all evaluators evaluated to {@code true}, {@code false} otherwise
     */
    public boolean validate(EvaluationContext ctx, JsonNode node) {
        boolean outOfDynamicScope = ctx.isOutOfDynamicScope(parentUri);
        if (outOfDynamicScope) {
            ctx.pushDynamicScope(parentUri);
        }

        int annotationsBefore = ctx.getEvaluationItems().size();
        boolean valid = true;
        for (EvaluatorWrapper evaluator : evaluators) {
            Result result = evaluator.evaluate(ctx, node);
            EvaluationItem evaluationItem = new EvaluationItem(
                    evaluator.getKeywordPath(), schemaLocation, node.getJsonPointer(),
                    evaluator.getKeyword(), result.isValid(), result.getAnnotation(), result.getError());
            ctx.addEvaluationItem(evaluationItem);
            valid = valid && result.isValid();
        }
        if (!valid) {
            ctx.truncateAnnotationsToSize(annotationsBefore);
        }
        if (outOfDynamicScope) {
            ctx.popDynamicContext();
        }
        return valid;
    }
}
