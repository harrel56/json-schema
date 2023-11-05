package dev.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;

import static dev.harrel.jsonschema.Keyword.*;

/**
 * {@code EvaluatorFactory} implementation that supports <a href="https://json-schema.org/draft/2019-09/schema">2019 draft</a> specification.
 */
class Draft2019EvaluatorFactory implements EvaluatorFactory {
    private final Draft2020EvaluatorFactory draft2020EvaluatorFactory = new Draft2020EvaluatorFactory();
    private final Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> evaluatorsMap;

    public Draft2019EvaluatorFactory() {
        Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> map = new HashMap<>();
        map.put(ITEMS, Items2019Evaluator::new);
        map.put(ADDITIONAL_ITEMS, AdditionalItemsEvaluator::new);
        map.put(RECURSIVE_REF, (ctx, node) -> new RecursiveRefEvaluator(node));
        this.evaluatorsMap = Collections.unmodifiableMap(map);
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        if (evaluatorsMap.containsKey(fieldName)) {
            try {
                return Optional.of(evaluatorsMap.get(fieldName).apply(ctx, node));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return draft2020EvaluatorFactory.create(ctx, fieldName, node);
    }
}
