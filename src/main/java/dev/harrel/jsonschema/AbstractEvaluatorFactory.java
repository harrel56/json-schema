package dev.harrel.jsonschema;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static dev.harrel.jsonschema.Keyword.*;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

abstract class AbstractEvaluatorFactory implements EvaluatorFactory {
    private final Set<String> ignoredKeywords;
    private final Map<String, EvaluatorInfo> evaluatorsMap;

    AbstractEvaluatorFactory(Set<String> ignoredKeywords, Map<String, EvaluatorInfo> evaluatorsMap) {
        this.ignoredKeywords = unmodifiableSet(ignoredKeywords);
        this.evaluatorsMap = unmodifiableMap(evaluatorsMap);
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        if (ignoredKeywords.contains(fieldName)) {
            return Optional.empty();
        }

        EvaluatorInfo evaluatorInfo = evaluatorsMap.get(fieldName);
        if (evaluatorInfo == null) {
            if (node.isString()) {
                return Optional.of(new AnnotationEvaluator(node.asString()));
            } else {
                return Optional.empty();
            }
        }

        if (evaluatorInfo.vocabulary != null && !ctx.getActiveVocabularies().contains(evaluatorInfo.vocabulary)) {
            return Optional.empty();
        }

        try {
            return Optional.of(evaluatorInfo.creator.apply(ctx, node));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    static Map<String, EvaluatorInfo> createDefaultEvaluatorsMap(String coreVocab,
                                                                 String applicatorVocab,
                                                                 String unevaluatedVocab,
                                                                 String validationVocab) {
        Map<String, EvaluatorInfo> map = new HashMap<>();
        map.put(REF, new EvaluatorInfo(coreVocab, RefEvaluator::new));

        map.put(TYPE, new EvaluatorInfo(validationVocab, (ctx, node) -> new TypeEvaluator(node)));
        map.put(CONST, new EvaluatorInfo(validationVocab, (ctx, node) -> new ConstEvaluator(node)));
        map.put(ENUM, new EvaluatorInfo(validationVocab, (ctx, node) -> new EnumEvaluator(node)));
        map.put(MULTIPLE_OF, new EvaluatorInfo(validationVocab, (ctx, node) -> new MultipleOfEvaluator(node)));
        map.put(MAXIMUM, new EvaluatorInfo(validationVocab, (ctx, node) -> new MaximumEvaluator(node)));
        map.put(EXCLUSIVE_MAXIMUM, new EvaluatorInfo(validationVocab, (ctx, node) -> new ExclusiveMaximumEvaluator(node)));
        map.put(MINIMUM, new EvaluatorInfo(validationVocab, (ctx, node) -> new MinimumEvaluator(node)));
        map.put(EXCLUSIVE_MINIMUM, new EvaluatorInfo(validationVocab, (ctx, node) -> new ExclusiveMinimumEvaluator(node)));
        map.put(MAX_LENGTH, new EvaluatorInfo(validationVocab, (ctx, node) -> new MaxLengthEvaluator(node)));
        map.put(MIN_LENGTH, new EvaluatorInfo(validationVocab, (ctx, node) -> new MinLengthEvaluator(node)));
        map.put(PATTERN, new EvaluatorInfo(validationVocab, (ctx, node) -> new PatternEvaluator(node)));
        map.put(MAX_ITEMS, new EvaluatorInfo(validationVocab, (ctx, node) -> new MaxItemsEvaluator(node)));
        map.put(MIN_ITEMS, new EvaluatorInfo(validationVocab, (ctx, node) -> new MinItemsEvaluator(node)));
        map.put(UNIQUE_ITEMS, new EvaluatorInfo(validationVocab, (ctx, node) -> new UniqueItemsEvaluator(node)));
        map.put(MAX_CONTAINS, new EvaluatorInfo(validationVocab, (ctx, node) -> new MaxContainsEvaluator(node)));
        map.put(MIN_CONTAINS, new EvaluatorInfo(validationVocab, (ctx, node) -> new MinContainsEvaluator(node)));
        map.put(MAX_PROPERTIES, new EvaluatorInfo(validationVocab, (ctx, node) -> new MaxPropertiesEvaluator(node)));
        map.put(MIN_PROPERTIES, new EvaluatorInfo(validationVocab, (ctx, node) -> new MinPropertiesEvaluator(node)));
        map.put(REQUIRED, new EvaluatorInfo(validationVocab, (ctx, node) -> new RequiredEvaluator(node)));
        map.put(DEPENDENT_REQUIRED, new EvaluatorInfo(validationVocab, (ctx, node) -> new DependentRequiredEvaluator(node)));

        map.put(CONTAINS, new EvaluatorInfo(applicatorVocab, ContainsEvaluator::new));
        map.put(ADDITIONAL_PROPERTIES, new EvaluatorInfo(applicatorVocab, AdditionalPropertiesEvaluator::new));
        map.put(PROPERTIES, new EvaluatorInfo(applicatorVocab, PropertiesEvaluator::new));
        map.put(PATTERN_PROPERTIES, new EvaluatorInfo(applicatorVocab, PatternPropertiesEvaluator::new));
        map.put(DEPENDENT_SCHEMAS, new EvaluatorInfo(applicatorVocab, DependentSchemasEvaluator::new));
        map.put(PROPERTY_NAMES, new EvaluatorInfo(applicatorVocab, PropertyNamesEvaluator::new));
        map.put(IF, new EvaluatorInfo(applicatorVocab, IfThenElseEvaluator::new));
        map.put(ALL_OF, new EvaluatorInfo(applicatorVocab, AllOfEvaluator::new));
        map.put(ANY_OF, new EvaluatorInfo(applicatorVocab, AnyOfEvaluator::new));
        map.put(ONE_OF, new EvaluatorInfo(applicatorVocab, OneOfEvaluator::new));
        map.put(NOT, new EvaluatorInfo(applicatorVocab, NotEvaluator::new));

        map.put(UNEVALUATED_ITEMS, new EvaluatorInfo(unevaluatedVocab, UnevaluatedItemsEvaluator::new));
        map.put(UNEVALUATED_PROPERTIES, new EvaluatorInfo(unevaluatedVocab, UnevaluatedPropertiesEvaluator::new));

        return map;
    }

    static class EvaluatorInfo {
        final String vocabulary;
        final BiFunction<SchemaParsingContext, JsonNode, Evaluator> creator;

        EvaluatorInfo(String vocabulary, BiFunction<SchemaParsingContext, JsonNode, Evaluator> creator) {
            this.vocabulary = vocabulary;
            this.creator = creator;
        }
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
