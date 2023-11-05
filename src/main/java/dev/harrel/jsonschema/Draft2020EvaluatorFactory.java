package dev.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;

import static dev.harrel.jsonschema.Keyword.*;

/**
 * {@code EvaluatorFactory} implementation that supports <a href="https://json-schema.org/draft/2020-12/schema">2020 draft</a> specification.
 */
public class Draft2020EvaluatorFactory extends AbstractEvaluatorFactory {
    public Draft2020EvaluatorFactory() {
        super(Arrays.asList(ID, SCHEMA, ANCHOR, DYNAMIC_ANCHOR, VOCABULARY, COMMENT, DEFS, THEN, ELSE));
    }

    @Override
    void configureEvaluatorsMap(Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> map) {
        map.put(ITEMS, ItemsEvaluator::new);
        map.put(PREFIX_ITEMS, PrefixItemsEvaluator::new);
        map.put(DYNAMIC_REF, (ctx, node) -> new DynamicRefEvaluator(node));
    }
}
