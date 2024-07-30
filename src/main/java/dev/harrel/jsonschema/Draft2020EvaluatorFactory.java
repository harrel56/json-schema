package dev.harrel.jsonschema;

import java.util.*;

import static dev.harrel.jsonschema.Keyword.*;
import static dev.harrel.jsonschema.Vocabulary.*;

/**
 * {@code EvaluatorFactory} implementation that supports <a href="https://json-schema.org/draft/2020-12/schema">2020 draft</a> specification.
 */
public class Draft2020EvaluatorFactory extends AbstractEvaluatorFactory {
    public Draft2020EvaluatorFactory() {
        super(getIgnoredKeywords(), createEvaluatorMap());
    }

    private static Set<String> getIgnoredKeywords() {
        return new HashSet<>(Arrays.asList(ID, SCHEMA, ANCHOR, DYNAMIC_ANCHOR, VOCABULARY, COMMENT, DEFS, THEN, ELSE));
    }

    private static Map<String, EvaluatorInfo> createEvaluatorMap() {
        Map<String, EvaluatorInfo> map = createDefaultEvaluatorsMap(Draft2020.CORE, Draft2020.APPLICATOR, Draft2020.UNEVALUATED, Draft2020.VALIDATION);
        map.put(DYNAMIC_REF, new EvaluatorInfo(Draft2020.CORE, (ctx, node) -> new DynamicRefEvaluator(node)));
        map.put(ITEMS, new EvaluatorInfo(Draft2020.APPLICATOR, ItemsEvaluator::new));
        map.put(PREFIX_ITEMS, new EvaluatorInfo(Draft2020.APPLICATOR, PrefixItemsEvaluator::new));
        return map;
    }
}
