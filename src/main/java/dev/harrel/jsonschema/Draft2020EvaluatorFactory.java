package dev.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;

public class Draft2020EvaluatorFactory implements EvaluatorFactory {

    protected final Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> evaluatorsMap;

    public Draft2020EvaluatorFactory() {
        Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> map = new HashMap<>();
        map.put(Keyword.TYPE, (ctx, node) -> new TypeEvaluator(node));
        map.put(Keyword.CONST, (ctx, node) -> new ConstEvaluator(node));
        map.put(Keyword.ENUM, (ctx, node) -> new EnumEvaluator(node));
        map.put(Keyword.MULTIPLE_OF, (ctx, node) -> new MultipleOfEvaluator(node));
        map.put(Keyword.MAXIMUM, (ctx, node) -> new MaximumEvaluator(node));
        map.put(Keyword.EXCLUSIVE_MAXIMUM, (ctx, node) -> new ExclusiveMaximumEvaluator(node));
        map.put(Keyword.MINIMUM, (ctx, node) -> new MinimumEvaluator(node));
        map.put(Keyword.EXCLUSIVE_MINIMUM, (ctx, node) -> new ExclusiveMinimumEvaluator(node));
        map.put(Keyword.MAX_LENGTH, (ctx, node) -> new MaxLengthEvaluator(node));
        map.put(Keyword.MIN_LENGTH, (ctx, node) -> new MinLengthEvaluator(node));
        map.put(Keyword.PATTERN, (ctx, node) -> new PatternEvaluator(node));
        map.put(Keyword.MAX_ITEMS, (ctx, node) -> new MaxItemsEvaluator(node));
        map.put(Keyword.MIN_ITEMS, (ctx, node) -> new MinItemsEvaluator(node));
        map.put(Keyword.UNIQUE_ITEMS, (ctx, node) -> new UniqueItemsEvaluator(node));
        map.put(Keyword.MAX_CONTAINS, MaxContainsEvaluator::new);
        map.put(Keyword.MIN_CONTAINS, MinContainsEvaluator::new);
        map.put(Keyword.MAX_PROPERTIES, (ctx, node) -> new MaxPropertiesEvaluator(node));
        map.put(Keyword.MIN_PROPERTIES, (ctx, node) -> new MinPropertiesEvaluator(node));
        map.put(Keyword.REQUIRED, (ctx, node) -> new RequiredEvaluator(node));
        map.put(Keyword.DEPENDENT_REQUIRED, (ctx, node) -> new DependentRequiredEvaluator(node));

        map.put(Keyword.PREFIX_ITEMS, PrefixItemsEvaluator::new);
        map.put(Keyword.ITEMS, ItemsEvaluator::new);
        map.put(Keyword.CONTAINS, ContainsEvaluator::new);
        map.put(Keyword.ADDITIONAL_PROPERTIES, AdditionalPropertiesEvaluator::new);
        map.put(Keyword.PROPERTIES, PropertiesEvaluator::new);
        map.put(Keyword.PATTERN_PROPERTIES, PatternPropertiesEvaluator::new);
        map.put(Keyword.DEPENDENT_SCHEMAS, DependentSchemasEvaluator::new);
        map.put(Keyword.PROPERTY_NAMES, PropertyNamesEvaluator::new);
        map.put(Keyword.IF, IfThenElseEvaluator::new);
        map.put(Keyword.ALL_OF, AllOfEvaluator::new);
        map.put(Keyword.ANY_OF, AnyOfEvaluator::new);
        map.put(Keyword.ONE_OF, OneOfEvaluator::new);
        map.put(Keyword.NOT, NotEvaluator::new);
        map.put(Keyword.UNEVALUATED_ITEMS, UnevaluatedItemsEvaluator::new);
        map.put(Keyword.UNEVALUATED_PROPERTIES, UnevaluatedPropertiesEvaluator::new);
        map.put(Keyword.REF, (ctx, node) -> new RefEvaluator(node));
        map.put(Keyword.DYNAMIC_REF, (ctx, node) -> new DynamicRefEvaluator(node));
        this.evaluatorsMap = Collections.unmodifiableMap(map);
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        try {
            return Optional.ofNullable(evaluatorsMap.get(fieldName))
                    .map(fun -> fun.apply(ctx, node));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
