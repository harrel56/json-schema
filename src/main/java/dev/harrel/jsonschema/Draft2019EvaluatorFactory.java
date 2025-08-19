package dev.harrel.jsonschema;

import java.util.*;

import static dev.harrel.jsonschema.Keyword.*;
import static dev.harrel.jsonschema.Vocabulary.*;

/**
 * {@code EvaluatorFactory} implementation that supports <a href="https://json-schema.org/draft/2019-09/schema">2019 draft</a> specification.
 */
public class Draft2019EvaluatorFactory extends AbstractEvaluatorFactory {
    public Draft2019EvaluatorFactory() {
        super(getIgnoredKeywords(), createEvaluatorMap());
    }

    private static Set<String> getIgnoredKeywords() {
        return new HashSet<>(Arrays.asList(ID, SCHEMA, ANCHOR, RECURSIVE_ANCHOR, VOCABULARY, COMMENT, DEFS, THEN, ELSE));
    }

    private static Map<String, EvaluatorInfo> createEvaluatorMap() {
        Map<String, EvaluatorInfo> map = createDefaultEvaluatorsMap(new VocabularyData(Draft2019.CORE, Draft2019.APPLICATOR,
                Draft2019.APPLICATOR, Draft2019.VALIDATION, Draft2019.META_DATA, Draft2019.CONTENT));
        map.put(RECURSIVE_REF, new EvaluatorInfo(Draft2019.CORE, (ctx, node) -> new RecursiveRefEvaluator(node)));
        map.put(ITEMS, new EvaluatorInfo(Draft2019.APPLICATOR, ItemsLegacyEvaluator::new));
        map.put(ADDITIONAL_ITEMS, new EvaluatorInfo(Draft2019.APPLICATOR, AdditionalItemsEvaluator::new));
        return map;
    }
}
