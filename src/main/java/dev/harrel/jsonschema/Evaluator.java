package dev.harrel.jsonschema;

/**
 * {@code Evaluator} interface is the main abstraction for the keyword evaluation logic.
 */
public interface Evaluator {
    /**
     * Evaluation logic for a specific keyword.
     * Must not throw any exceptions, any possible evaluation failures should be reflected by return object.
     * @param ctx current evaluation context
     * @param node JSON node on which the evaluation should act upon
     * @return evaluation result
     */
    EvaluationResult evaluate(EvaluationContext ctx, JsonNode node);

    /**
     * Order of {@code Evaluator} determines order of evaluators execution in scope of single schema location.
     * By default, evaluators are executed in order of their occurrence in JSON object. If {@code Evaluator}
     * is required to be run before or after other evaluators, manipulating order value is the only way of achieving
     * this behaviour.
     * @return order
     */
    default int getOrder() {
        return 0;
    }
}
