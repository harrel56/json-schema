package dev.harrel.jsonschema;

import java.util.*;

import static dev.harrel.jsonschema.Keyword.*;

/**
 * {@code EvaluatorFactory} implementation that supports <a href="https://json-schema.org/draft-07/schema">draft 7</a> specification.
 */
public class Draft7EvaluatorFactory extends AbstractEvaluatorFactory {
    public Draft7EvaluatorFactory() {
        super(getIgnoredKeywords(), createEvaluatorMap());
    }

    private static Set<String> getIgnoredKeywords() {
        return new HashSet<>(Arrays.asList(ID, SCHEMA, COMMENT, DEFINITIONS, THEN, ELSE));
    }

    private static Map<String, EvaluatorInfo> createEvaluatorMap() {
        Map<String, EvaluatorInfo> map = createDefaultEvaluatorsMap(new VocabularyData());
        map.put(ITEMS, new EvaluatorInfo(null, ItemsLegacyEvaluator::new));
        map.put(ADDITIONAL_ITEMS, new EvaluatorInfo(null, AdditionalItemsEvaluator::new));
        map.put(DEPENDENCIES, new EvaluatorInfo(null, DependenciesLegacyEvaluator::new));
        map.put(REF, new EvaluatorInfo(null, LegacyRefEvaluator::new));
        map.remove(MAX_CONTAINS);
        map.remove(MIN_CONTAINS);
        map.remove(DEPENDENT_REQUIRED);
        map.remove(DEPENDENT_SCHEMAS);
        map.remove(UNEVALUATED_ITEMS);
        map.remove(UNEVALUATED_PROPERTIES);
        return map;
    }
}
