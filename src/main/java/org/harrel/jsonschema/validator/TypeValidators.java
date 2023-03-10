package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Result;
import org.harrel.jsonschema.ValidationContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class TypeValidators {

    private TypeValidators() {}

    private static final String ERROR_MESSAGE = "Invalid type. Expected %s.";

    private static final Map<SimpleType, Validator> TYPE_CHECKS = Map.of(
            SimpleType.NULL, (ctx, node) -> node.isNull() ? Result.success() : Result.failure(ERROR_MESSAGE.formatted(SimpleType.NULL)),
            SimpleType.BOOLEAN, (ctx, node) -> node.isBoolean() ? Result.success() : Result.failure(ERROR_MESSAGE.formatted(SimpleType.BOOLEAN)),
            SimpleType.STRING, (ctx, node) -> node.isString() ? Result.success() : Result.failure(ERROR_MESSAGE.formatted(SimpleType.STRING)),
            SimpleType.INTEGER, (ctx, node) -> node.isInteger() ? Result.success() : Result.failure(ERROR_MESSAGE.formatted(SimpleType.INTEGER)),
            SimpleType.NUMBER, (ctx, node) -> node.isNumber() ? Result.success() : Result.failure(ERROR_MESSAGE.formatted(SimpleType.NUMBER)),
            SimpleType.ARRAY, (ctx, node) -> node.isArray() ? Result.success() : Result.failure(ERROR_MESSAGE.formatted(SimpleType.ARRAY)),
            SimpleType.OBJECT, (ctx, node) -> node.isObject() ? Result.success() : Result.failure(ERROR_MESSAGE.formatted(SimpleType.OBJECT))
    );

    public static Validator getTypeCheck(JsonNode node) {
        if (node.isString()) {
            return TYPE_CHECKS.get(SimpleType.fromName(node.asString()));
        }

        List<SimpleType> types = node.asArray().stream()
                .map(JsonNode::asString)
                .map(SimpleType::fromName)
                .toList();
        return new TypeArrayValidator(types);
    }

    private static class TypeArrayValidator implements Validator {
        private final List<SimpleType> types;

        public TypeArrayValidator(List<SimpleType> types) {
            this.types = Collections.unmodifiableList(types);
        }

        @Override
        public ValidationResult validate(ValidationContext ctx, JsonNode node) {
            boolean invalid = types.stream()
                    .map(TYPE_CHECKS::get)
                    .map(validator -> validator.validate(ctx, node))
                    .noneMatch(ValidationResult::isValid);
            return invalid ? Result.failure("Invalid type. Expected any of %s.".formatted(types)) : Result.success();
        }
    }
}


