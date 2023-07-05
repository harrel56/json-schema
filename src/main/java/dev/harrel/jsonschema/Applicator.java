package dev.harrel.jsonschema;

import java.util.Set;

import static dev.harrel.jsonschema.Vocabulary.APPLICATOR_VOCABULARY;

interface Applicator extends Evaluator {

    @Override
    default Result evaluate(EvaluationContext ctx, JsonNode node) {
        return apply(ctx, node) ? Result.success() : Result.failure();
    }

    @Override
    default Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    boolean apply(EvaluationContext ctx, JsonNode node);
}
