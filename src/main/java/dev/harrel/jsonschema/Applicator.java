package dev.harrel.jsonschema;

import java.util.Set;

import static dev.harrel.jsonschema.Vocabulary.APPLICATOR_VOCABULARY;

interface Applicator extends Evaluator {

    @Override
    Result evaluate(EvaluationContext ctx, JsonNode node);

    @Override
    default Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }
}
