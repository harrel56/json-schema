package dev.harrel.jsonschema;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static dev.harrel.jsonschema.Keyword.*;

/**
 * {@code EvaluatorFactory} implementation that supports <a href="https://json-schema.org/draft-06/schema">draft 6</a> specification.
 */
public class Draft6EvaluatorFactory extends AbstractEvaluatorFactory {
    public Draft6EvaluatorFactory() {
        super(getIgnoredKeywords(), createEvaluatorMap());
    }

    private static Set<String> getIgnoredKeywords() {
        return new HashSet<>(Arrays.asList(ID, SCHEMA, DEFINITIONS));
    }

    private static Map<String, EvaluatorInfo> createEvaluatorMap() {
        Map<String, EvaluatorInfo> map = createDefaultEvaluatorsMap(new VocabularyData());
        map.put(ITEMS, new EvaluatorInfo(null, ItemsLegacyEvaluator::new));
        map.put(ADDITIONAL_ITEMS, new EvaluatorInfo(null, AdditionalItemsEvaluator::new));
        map.put(DEPENDENCIES, new EvaluatorInfo(null, DependenciesLegacyEvaluator::new));
        map.put(REF, new EvaluatorInfo(null, LegacyRefEvaluator::new));
        map.remove(IF);
        map.remove(THEN);
        map.remove(ELSE);
        map.remove(MAX_CONTAINS);
        map.remove(MIN_CONTAINS);
        map.remove(DEPENDENT_REQUIRED);
        map.remove(DEPENDENT_SCHEMAS);
        map.remove(UNEVALUATED_ITEMS);
        map.remove(UNEVALUATED_PROPERTIES);
        return map;
    }
}
