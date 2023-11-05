package dev.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;

import static dev.harrel.jsonschema.Keyword.*;

/**
 * {@code EvaluatorFactory} implementation that supports <a href="https://json-schema.org/draft/2019-09/schema">2019 draft</a> specification.
 */
public class Draft2019EvaluatorFactory extends AbstractEvaluatorFactory {
    public Draft2019EvaluatorFactory() {
        super(Arrays.asList(ID, SCHEMA, ANCHOR, RECURSIVE_ANCHOR, VOCABULARY, COMMENT, DEFS, THEN, ELSE));
    }

    @Override
    void configureEvaluatorsMap(Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> map) {
        map.put(ITEMS, Items2019Evaluator::new);
        map.put(ADDITIONAL_ITEMS, AdditionalItemsEvaluator::new);
        map.put(RECURSIVE_REF, (ctx, node) -> new RecursiveRefEvaluator(node));
    }
}
