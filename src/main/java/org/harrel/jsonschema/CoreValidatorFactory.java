package org.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;

public class CoreValidatorFactory implements ValidatorFactory {

    private final Map<String, BiFunction<SchemaParsingContext, JsonNode, Validator>> validatorsMap;

    public CoreValidatorFactory() {
        Map<String, BiFunction<SchemaParsingContext, JsonNode, Validator>> map = new HashMap<>();
        map.put(Keyword.TYPE, (ctx, node) -> new TypeValidator(node));
        map.put(Keyword.CONST, (ctx, node) -> new ConstValidator(node));
        map.put(Keyword.ENUM, (ctx, node) -> new EnumValidator(node));
        map.put(Keyword.MULTIPLE_OF, (ctx, node) -> new MultipleOfValidator(node));
        map.put(Keyword.MAXIMUM, (ctx, node) -> new MaximumValidator(node));
        map.put(Keyword.EXCLUSIVE_MAXIMUM, (ctx, node) -> new ExclusiveMaximumValidator(node));
        map.put(Keyword.MINIMUM, (ctx, node) -> new MinimumValidator(node));
        map.put(Keyword.EXCLUSIVE_MINIMUM, (ctx, node) -> new ExclusiveMinimumValidator(node));
        map.put(Keyword.MAX_LENGTH, (ctx, node) -> new MaxLengthValidator(node));
        map.put(Keyword.MIN_LENGTH, (ctx, node) -> new MinLengthValidator(node));
        map.put(Keyword.PATTERN, (ctx, node) -> new PatternValidator(node));
        map.put(Keyword.MAX_ITEMS, (ctx, node) -> new MaxItemsValidator(node));
        map.put(Keyword.MIN_ITEMS, (ctx, node) -> new MinItemsValidator(node));
        map.put(Keyword.UNIQUE_ITEMS, (ctx, node) -> new UniqueItemsValidator(node));
        map.put(Keyword.MAX_CONTAINS, MaxContainsValidator::new);
        map.put(Keyword.MIN_CONTAINS, MinContainsValidator::new);
        map.put(Keyword.MAX_PROPERTIES, (ctx, node) -> new MaxPropertiesValidator(node));
        map.put(Keyword.MIN_PROPERTIES, (ctx, node) -> new MinPropertiesValidator(node));
        map.put(Keyword.REQUIRED, (ctx, node) -> new RequiredValidator(node));
        map.put(Keyword.DEPENDENT_REQUIRED, (ctx, node) -> new DependentRequiredValidator(node));

        map.put(Keyword.PREFIX_ITEMS, PrefixItemsValidator::new);
        map.put(Keyword.ITEMS, ItemsValidator::new);
        map.put(Keyword.CONTAINS, ContainsValidator::new);
        map.put(Keyword.ADDITIONAL_PROPERTIES, AdditionalPropertiesValidator::new);
        map.put(Keyword.PROPERTIES, PropertiesValidator::new);
        map.put(Keyword.PATTERN_PROPERTIES, PatternPropertiesValidator::new);
        map.put(Keyword.DEPENDENT_SCHEMAS, DependentSchemasValidator::new);
        map.put(Keyword.PROPERTY_NAMES, PropertyNamesValidator::new);
        map.put(Keyword.IF, IfThenElseValidator::new);
        map.put(Keyword.ALL_OF, AllOfValidator::new);
        map.put(Keyword.ANY_OF, AnyOfValidator::new);
        map.put(Keyword.ONE_OF, OneOfValidator::new);
        map.put(Keyword.NOT, NotValidator::new);
        map.put(Keyword.UNEVALUATED_ITEMS, UnevaluatedItemsValidator::new);
        map.put(Keyword.UNEVALUATED_PROPERTIES, UnevaluatedPropertiesValidator::new);
        map.put(Keyword.REF, (ctx, node) -> new RefValidator(node));
        map.put(Keyword.DYNAMIC_REF, (ctx, node) -> new DynamicRefValidator(node));
        this.validatorsMap = Collections.unmodifiableMap(map);
    }

    @Override
    public Optional<Validator> create(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        try {
            return Optional.ofNullable(validatorsMap.get(fieldName))
                    .map(fun -> fun.apply(ctx, node));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
