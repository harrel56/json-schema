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
                return Optional.of(new AnnotationEvaluator(node));
            } else {
                return Optional.empty();
            }
        }

        if (evaluatorInfo.vocabulary != null && !ctx.getActiveVocabularies().contains(evaluatorInfo.vocabulary)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(evaluatorInfo.creator.apply(ctx, node));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    static Map<String, EvaluatorInfo> createDefaultEvaluatorsMap(VocabularyData data) {
        Map<String, EvaluatorInfo> map = new HashMap<>();
        map.put(REF, new EvaluatorInfo(data.coreVocab, RefEvaluator::new));

        map.put(TYPE, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new TypeEvaluator(node)));
        map.put(CONST, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new ConstEvaluator(node)));
        map.put(ENUM, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new EnumEvaluator(node)));
        map.put(MULTIPLE_OF, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MultipleOfEvaluator(node)));
        map.put(MAXIMUM, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MaximumEvaluator(node)));
        map.put(EXCLUSIVE_MAXIMUM, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new ExclusiveMaximumEvaluator(node)));
        map.put(MINIMUM, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MinimumEvaluator(node)));
        map.put(EXCLUSIVE_MINIMUM, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new ExclusiveMinimumEvaluator(node)));
        map.put(MAX_LENGTH, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MaxLengthEvaluator(node)));
        map.put(MIN_LENGTH, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MinLengthEvaluator(node)));
        map.put(PATTERN, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new PatternEvaluator(node)));
        map.put(MAX_ITEMS, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MaxItemsEvaluator(node)));
        map.put(MIN_ITEMS, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MinItemsEvaluator(node)));
        map.put(UNIQUE_ITEMS, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new UniqueItemsEvaluator(node)));
        map.put(MAX_CONTAINS, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MaxContainsEvaluator(node)));
        map.put(MIN_CONTAINS, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MinContainsEvaluator(node)));
        map.put(MAX_PROPERTIES, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MaxPropertiesEvaluator(node)));
        map.put(MIN_PROPERTIES, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new MinPropertiesEvaluator(node)));
        map.put(REQUIRED, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new RequiredEvaluator(node)));
        map.put(DEPENDENT_REQUIRED, new EvaluatorInfo(data.validationVocab, (ctx, node) -> new DependentRequiredEvaluator(node)));

        map.put(CONTAINS, new EvaluatorInfo(data.applicatorVocab, ContainsEvaluator::new));
        map.put(ADDITIONAL_PROPERTIES, new EvaluatorInfo(data.applicatorVocab, AdditionalPropertiesEvaluator::new));
        map.put(PROPERTIES, new EvaluatorInfo(data.applicatorVocab, PropertiesEvaluator::new));
        map.put(PATTERN_PROPERTIES, new EvaluatorInfo(data.applicatorVocab, PatternPropertiesEvaluator::new));
        map.put(DEPENDENT_SCHEMAS, new EvaluatorInfo(data.applicatorVocab, DependentSchemasEvaluator::new));
        map.put(PROPERTY_NAMES, new EvaluatorInfo(data.applicatorVocab, PropertyNamesEvaluator::new));
        map.put(IF, new EvaluatorInfo(data.applicatorVocab, IfThenElseEvaluator::new));
        map.put(ALL_OF, new EvaluatorInfo(data.applicatorVocab, AllOfEvaluator::new));
        map.put(ANY_OF, new EvaluatorInfo(data.applicatorVocab, AnyOfEvaluator::new));
        map.put(ONE_OF, new EvaluatorInfo(data.applicatorVocab, OneOfEvaluator::new));
        map.put(NOT, new EvaluatorInfo(data.applicatorVocab, NotEvaluator::new));

        map.put(UNEVALUATED_ITEMS, new EvaluatorInfo(data.unevaluatedVocab, UnevaluatedItemsEvaluator::new));
        map.put(UNEVALUATED_PROPERTIES, new EvaluatorInfo(data.unevaluatedVocab, UnevaluatedPropertiesEvaluator::new));

        map.put(TITLE, new EvaluatorInfo(data.metaDataVocab,
                (ctx, node) -> node.isString() ? new AnnotationEvaluator(node) : null));
        map.put(DESCRIPTION, new EvaluatorInfo(data.metaDataVocab,
                (ctx, node) -> node.isString() ? new AnnotationEvaluator(node) : null));
        map.put(DEFAULT, new EvaluatorInfo(data.metaDataVocab, (ctx, node) -> new AnnotationEvaluator(node)));
        map.put(DEPRECATED, new EvaluatorInfo(data.metaDataVocab,
                (ctx, node) -> node.isBoolean() ? new AnnotationEvaluator(node) : null));
        map.put(READ_ONLY, new EvaluatorInfo(data.metaDataVocab,
                (ctx, node) -> node.isBoolean() ? new AnnotationEvaluator(node) : null));
        map.put(WRITE_ONLY, new EvaluatorInfo(data.metaDataVocab,
                (ctx, node) -> node.isBoolean() ? new AnnotationEvaluator(node) : null));
        map.put(EXAMPLES, new EvaluatorInfo(data.metaDataVocab,
                (ctx, node) -> node.isArray() ? new AnnotationEvaluator(node) : null));

        map.put(CONTENT_MEDIA_TYPE, new EvaluatorInfo(data.contentVocab,
                (ctx, node) -> node.isString() ? new ContentAnnotationEvaluator(node) : null));
        map.put(CONTENT_ENCODING, new EvaluatorInfo(data.contentVocab,
                (ctx, node) -> node.isString() ? new ContentAnnotationEvaluator(node) : null));
        map.put(CONTENT_SCHEMA, new EvaluatorInfo(data.contentVocab, (ctx, node) -> {
            if (ctx.getCurrentSchemaObject().containsKey(CONTENT_MEDIA_TYPE)) {
                return new ContentAnnotationEvaluator(node);
            } else {
                return null;
            }
        }));

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

    static class VocabularyData {
        final String coreVocab;
        final String applicatorVocab;
        final String unevaluatedVocab;
        final String validationVocab;
        final String metaDataVocab;
        final String contentVocab;

        VocabularyData() {
            this(null, null, null, null, null, null);
        }

        VocabularyData(String coreVocab,
                       String applicatorVocab,
                       String unevaluatedVocab,
                       String validationVocab,
                       String metaDataVocab,
                       String contentVocab) {
            this.coreVocab = coreVocab;
            this.applicatorVocab = applicatorVocab;
            this.unevaluatedVocab = unevaluatedVocab;
            this.validationVocab = validationVocab;
            this.metaDataVocab = metaDataVocab;
            this.contentVocab = contentVocab;
        }
    }

    static class AnnotationEvaluator implements Evaluator {
        final Object annotation;

        public AnnotationEvaluator(JsonNode node) {
            this.annotation = JsonNodeUtil.getValue(node);
        }

        @Override
        public Result evaluate(EvaluationContext ctx, JsonNode node) {
            return Result.success(annotation);
        }
    }

    static class ContentAnnotationEvaluator extends AnnotationEvaluator {
        ContentAnnotationEvaluator(JsonNode node) {
            super(node);
        }

        @Override
        public Result evaluate(EvaluationContext ctx, JsonNode node) {
            if (node.isString()) {
                return Result.success(annotation);
            } else {
                return Result.success();
            }
        }
    }
}
