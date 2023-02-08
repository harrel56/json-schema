package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

import java.util.*;
import java.util.function.BiFunction;

public class ValidatorFactory {

    private final Map<String, BiFunction<SchemaParsingContext, JsonNode, Validator>> validatorsMap;

    public ValidatorFactory() {
        Map<String, BiFunction<SchemaParsingContext, JsonNode, Validator>> map = new HashMap<>();
        map.put("$ref", (ctx, node) -> new RefValidator(node));
        map.put("anyOf", AnyOfValidator::new);
        map.put("properties", PropertiesValidator::new);
        map.put("required", (ctx, node) -> new RequiredValidator(node));
        map.put("type", (ctx, node) -> TypeValidators.getTypeCheck(node));
        map.put("const", (ctx, node) -> new ConstValidator(node));
        map.put("enum", (ctx, node) -> new EnumValidator(node));
        this.validatorsMap = Collections.unmodifiableMap(map);
    }

    public Optional<Validator> fromField(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        return Optional.ofNullable(validatorsMap.get(fieldName))
                .map(fun -> fun.apply(ctx, node));
    }
}
