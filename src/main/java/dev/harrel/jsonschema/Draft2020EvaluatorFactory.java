package dev.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;

import static dev.harrel.jsonschema.Keyword.*;

/**
 * {@code EvaluatorFactory} implementation that supports <a href="https://json-schema.org/draft/2020-12/schema">2020 draft</a> specification.
 */
public class Draft2020EvaluatorFactory implements EvaluatorFactory {
    private static final Set<String> IGNORED_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(ID, SCHEMA, ANCHOR, DYNAMIC_ANCHOR, VOCABULARY, COMMENT, DEFS, THEN, ELSE)));

    protected final Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> evaluatorsMap;

    public Draft2020EvaluatorFactory() {
        Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> map = new HashMap<>();
        map.put(TYPE, (ctx, node) -> new TypeEvaluator(node));
        map.put(CONST, (ctx, node) -> new ConstEvaluator(node));
        map.put(ENUM, (ctx, node) -> new EnumEvaluator(node));
        map.put(MULTIPLE_OF, (ctx, node) -> new MultipleOfEvaluator(node));
        map.put(MAXIMUM, (ctx, node) -> new MaximumEvaluator(node));
        map.put(EXCLUSIVE_MAXIMUM, (ctx, node) -> new ExclusiveMaximumEvaluator(node));
        map.put(MINIMUM, (ctx, node) -> new MinimumEvaluator(node));
        map.put(EXCLUSIVE_MINIMUM, (ctx, node) -> new ExclusiveMinimumEvaluator(node));
        map.put(MAX_LENGTH, (ctx, node) -> new MaxLengthEvaluator(node));
        map.put(MIN_LENGTH, (ctx, node) -> new MinLengthEvaluator(node));
        map.put(PATTERN, (ctx, node) -> new PatternEvaluator(node));
        map.put(MAX_ITEMS, (ctx, node) -> new MaxItemsEvaluator(node));
        map.put(MIN_ITEMS, (ctx, node) -> new MinItemsEvaluator(node));
        map.put(UNIQUE_ITEMS, (ctx, node) -> new UniqueItemsEvaluator(node));
        map.put(MAX_CONTAINS, (ctx, node) -> new MaxContainsEvaluator(node));
        map.put(MIN_CONTAINS, (ctx, node) -> new MinContainsEvaluator(node));
        map.put(MAX_PROPERTIES, (ctx, node) -> new MaxPropertiesEvaluator(node));
        map.put(MIN_PROPERTIES, (ctx, node) -> new MinPropertiesEvaluator(node));
        map.put(REQUIRED, (ctx, node) -> new RequiredEvaluator(node));
        map.put(DEPENDENT_REQUIRED, (ctx, node) -> new DependentRequiredEvaluator(node));

        map.put(PREFIX_ITEMS, PrefixItemsEvaluator::new);
        map.put(ITEMS, ItemsEvaluator::new);
        map.put(CONTAINS, ContainsEvaluator::new);
        map.put(ADDITIONAL_PROPERTIES, AdditionalPropertiesEvaluator::new);
        map.put(PROPERTIES, PropertiesEvaluator::new);
        map.put(PATTERN_PROPERTIES, PatternPropertiesEvaluator::new);
        map.put(DEPENDENT_SCHEMAS, DependentSchemasEvaluator::new);
        map.put(PROPERTY_NAMES, PropertyNamesEvaluator::new);
        map.put(IF, IfThenElseEvaluator::new);
        map.put(ALL_OF, AllOfEvaluator::new);
        map.put(ANY_OF, AnyOfEvaluator::new);
        map.put(ONE_OF, OneOfEvaluator::new);
        map.put(NOT, NotEvaluator::new);
        map.put(UNEVALUATED_ITEMS, UnevaluatedItemsEvaluator::new);
        map.put(UNEVALUATED_PROPERTIES, UnevaluatedPropertiesEvaluator::new);
        map.put(REF, (ctx, node) -> new RefEvaluator(node));
        map.put(DYNAMIC_REF, (ctx, node) -> new DynamicRefEvaluator(node));
        this.evaluatorsMap = Collections.unmodifiableMap(map);
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        if (IGNORED_KEYWORDS.contains(fieldName)) {
            return Optional.empty();
        }
        if (evaluatorsMap.containsKey(fieldName)) {
            try {
                return Optional.of(evaluatorsMap.get(fieldName).apply(ctx, node));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        if (node.isString()) {
            return Optional.of(new AnnotationEvaluator(node.asString()));
        }
        return Optional.empty();
    }

    static class AnnotationEvaluator implements Evaluator {
        private final String annotation;

        public AnnotationEvaluator(String annotation) {
            this.annotation = annotation;
        }

        @Override
        public Result evaluate(EvaluationContext ctx, JsonNode node) {
            return Result.success(annotation);
        }
    }
}
