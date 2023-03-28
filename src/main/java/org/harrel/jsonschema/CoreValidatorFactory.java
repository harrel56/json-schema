package org.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;

public class CoreValidatorFactory implements ValidatorFactory {

    private final Map<String, BiFunction<SchemaParsingContext, JsonNode, Validator>> validatorsMap;

    public CoreValidatorFactory() {
        Map<String, BiFunction<SchemaParsingContext, JsonNode, Validator>> map = new HashMap<>();
        map.put("$ref", (ctx, node) -> new RefValidator(node));
        map.put("$dynamicRef", (ctx, node) -> new DynamicRefValidator(node));
        map.put("if", IfThenElseValidator::new);
        map.put("anyOf", AnyOfValidator::new);
        map.put("allOf", AllOfValidator::new);
        map.put("oneOf", OneOfValidator::new);
        map.put("not", NotValidator::new);
        map.put("items", ItemsValidator::new);
        map.put("prefixItems", PrefixItemsValidator::new);
        map.put("maxItems", (ctx, node) -> new MaxItemsValidator(node));
        map.put("minItems", (ctx, node) -> new MinItemsValidator(node));
        map.put("uniqueItems", (ctx, node) -> new UniqueItemsValidator(node));
        map.put("contains", ContainsValidator::new);
        map.put("maxContains", MaxContainsValidator::new);
        map.put("minContains", MinContainsValidator::new);
        map.put("unevaluatedItems", UnevaluatedItemsValidator::new);
        map.put("properties", PropertiesValidator::new);
        map.put("additionalProperties", AdditionalPropertiesValidator::new);
        map.put("patternProperties", PatternPropertiesValidator::new);
        map.put("propertyNames", PropertyNamesValidator::new);
        map.put("unevaluatedProperties", UnevaluatedPropertiesValidator::new);
        map.put("maxProperties", (ctx, node) -> new MaxPropertiesValidator(node));
        map.put("minProperties", (ctx, node) -> new MinPropertiesValidator(node));
        map.put("required", (ctx, node) -> new RequiredValidator(node));
        map.put("dependentRequired", (ctx, node) -> new DependentRequiredValidator(node));
        map.put("dependentSchemas", DependentSchemasValidator::new);
        map.put("type", (ctx, node) -> new TypeValidator(node));
        map.put("const", (ctx, node) -> new ConstValidator(node));
        map.put("enum", (ctx, node) -> new EnumValidator(node));
        map.put("maxLength", (ctx, node) -> new MaxLengthValidator(node));
        map.put("minLength", (ctx, node) -> new MinLengthValidator(node));
        map.put("pattern", (ctx, node) -> new PatternValidator(node));
        map.put("maximum", (ctx, node) -> new MaximumValidator(node));
        map.put("minimum", (ctx, node) -> new MinimumValidator(node));
        map.put("exclusiveMaximum", (ctx, node) -> new ExclusiveMaximumValidator(node));
        map.put("exclusiveMinimum", (ctx, node) -> new ExclusiveMinimumValidator(node));
        map.put("multipleOf", (ctx, node) -> new MultipleOfValidator(node));
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
