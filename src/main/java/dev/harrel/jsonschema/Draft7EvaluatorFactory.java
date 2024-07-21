package dev.harrel.jsonschema;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

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
        Map<String, EvaluatorInfo> map = createDefaultEvaluatorsMap(null, null, null, null);
        // todo adjust evaluators
        return map;
    }
}
