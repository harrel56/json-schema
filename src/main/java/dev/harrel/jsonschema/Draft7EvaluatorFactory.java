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
        Map<String, EvaluatorInfo> map = createDefaultEvaluatorsMap(null, null, null, null);
        map.put(ITEMS, new EvaluatorInfo(null, ItemsLegacyEvaluator::new));
        map.put(ADDITIONAL_ITEMS, new EvaluatorInfo(null, AdditionalItemsEvaluator::new));
        return map;
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        if (!REF.equals(fieldName) && ctx.getCurrentSchemaObject().containsKey(REF)) {
            return Optional.empty();
        }
        return super.create(ctx, fieldName, node);
    }
}
