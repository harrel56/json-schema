package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Result;
import org.harrel.jsonschema.SimpleType;
import org.harrel.jsonschema.ValidationContext;

import java.util.Set;
import java.util.stream.Collectors;

class TypeValidator implements Validator {
    private final Set<SimpleType> types;

    TypeValidator(JsonNode node) {
        if (node.isString()) {
            this.types = Set.of(SimpleType.fromName(node.asString()));
        } else {
            this.types = node.asArray().stream()
                    .map(JsonNode::asString)
                    .map(SimpleType::fromName)
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        SimpleType nodeType = node.getNodeType();
        if (types.contains(nodeType) || nodeType == SimpleType.INTEGER && types.contains(SimpleType.NUMBER)) {
            return Result.success();
        } else {
            return Result.failure("Value is \"%s\" but should be \"%s\"".formatted(nodeType.getName(), types));
        }
    }
}

class ConstValidator implements Validator {
    private final JsonNode constNode;

    ConstValidator(JsonNode node) {
        this.constNode = node;
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        return constNode.isEqualTo(node) ? Result.success() : Result.failure("Expected " + constNode.toPrintableString());
    }
}
