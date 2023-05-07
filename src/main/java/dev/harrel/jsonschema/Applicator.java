package dev.harrel.jsonschema;

interface Applicator extends Evaluator {

    @Override
    default Result evaluate(EvaluationContext ctx, JsonNode node) {
        return apply(ctx, node) ? Result.success() : Result.failure();
    }

    boolean apply(EvaluationContext ctx, JsonNode node);
}
