package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Result;
import org.harrel.jsonschema.SimpleType;
import org.harrel.jsonschema.ValidationContext;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
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
            return Result.failure("Value is [%s] but should be [%s]".formatted(nodeType.getName(), types));
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

class EnumValidator implements Validator {
    private final List<JsonNode> enumNodes;

    EnumValidator(JsonNode node) {
        this.enumNodes = Collections.unmodifiableList(node.asArray());
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (enumNodes.stream().anyMatch(node::isEqualTo)) {
            return Result.success();
        } else {
            return Result.failure("Expected any of [%s]".formatted(enumNodes.stream().map(JsonNode::toPrintableString).toList()));
        }
    }
}

class MultipleOfValidator implements Validator {
    private final BigDecimal factor;

    MultipleOfValidator(JsonNode node) {
        this.factor = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return Result.success();
        }

        if (node.asNumber().remainder(factor).doubleValue() == 0.0) {
            return Result.success();
        } else {
            return Result.failure("%s is not multiple of %s".formatted(node.asNumber(), factor));
        }
    }
}

class MaximumValidator implements Validator {
    private final BigDecimal max;

    MaximumValidator(JsonNode node) {
        this.max = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return Result.success();
        }

        if (node.asNumber().compareTo(max) <= 0) {
            return Result.success();
        } else {
            return Result.failure("%s is greater than %s".formatted(node.asNumber(), max));
        }
    }
}

class ExclusiveMaximumValidator implements Validator {
    private final BigDecimal max;

    ExclusiveMaximumValidator(JsonNode node) {
        this.max = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return Result.success();
        }

        if (node.asNumber().compareTo(max) < 0) {
            return Result.success();
        } else {
            return Result.failure("%s is greater or equal to %s".formatted(node.asNumber(), max));
        }
    }
}

class MinimumValidator implements Validator {
    private final BigDecimal min;

    MinimumValidator(JsonNode node) {
        this.min = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return Result.success();
        }

        if (node.asNumber().compareTo(min) >= 0) {
            return Result.success();
        } else {
            return Result.failure("%s is lesser than %s".formatted(node.asNumber(), min));
        }
    }
}

class ExclusiveMinimumValidator implements Validator {
    private final BigDecimal min;

    ExclusiveMinimumValidator(JsonNode node) {
        this.min = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return Result.success();
        }

        if (node.asNumber().compareTo(min) > 0) {
            return Result.success();
        } else {
            return Result.failure("%s is lesser or equal to %s".formatted(node.asNumber(), min));
        }
    }
}

class MaxLengthValidator implements Validator {
    private final int maxLength;

    MaxLengthValidator(JsonNode node) {
        this.maxLength = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return Result.success();
        }

        String string = node.asString();
        if (string.codePointCount(0, string.length()) <= maxLength) {
            return Result.success();
        } else {
            return Result.failure("\"%s\" is longer than %d characters".formatted(string, maxLength));
        }
    }
}

class MinLengthValidator implements Validator {
    private final int minLength;

    MinLengthValidator(JsonNode node) {
        this.minLength = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return Result.success();
        }

        String string = node.asString();
        if (string.codePointCount(0, string.length()) >= minLength) {
            return Result.success();
        } else {
            return Result.failure("\"%s\" is shorter than %d characters".formatted(string, minLength));
        }
    }
}

class PatternValidator implements Validator {
    private final Pattern pattern;

    PatternValidator(JsonNode node) {
        this.pattern = Pattern.compile(node.asString());
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return Result.success();
        }

        if (pattern.matcher(node.asString()).find()) {
            return Result.success();
        } else {
            return Result.failure("\"%s\" does not match regular expression [%s]".formatted(node.asString(), pattern.toString()));
        }
    }
}

class MaxItemsValidator implements Validator {
    private final int maxItems;

    MaxItemsValidator(JsonNode node) {
        this.maxItems = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        if (node.asArray().size() <= maxItems) {
            return Result.success();
        } else {
            return Result.failure("Array has more than %d items".formatted(maxItems));
        }
    }
}

class MinItemsValidator implements Validator {
    private final int minItems;

    MinItemsValidator(JsonNode node) {
        this.minItems = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        if (node.asArray().size() >= minItems) {
            return Result.success();
        } else {
            return Result.failure("Array has less than %d items".formatted(minItems));
        }
    }
}