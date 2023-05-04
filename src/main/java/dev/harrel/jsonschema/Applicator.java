package dev.harrel.jsonschema;

interface Applicator extends Evaluator {

    @Override
    default EvaluationResult evaluate(EvaluationContext ctx, JsonNode node) {
        return apply(ctx, node) ? EvaluationResult.success() : EvaluationResult.failure();
    }

    boolean apply(EvaluationContext ctx, JsonNode node);
}
