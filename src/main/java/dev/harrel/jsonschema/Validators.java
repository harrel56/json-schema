package dev.harrel.jsonschema;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class TypeValidator implements Validator {
    private final Set<SimpleType> types;

    TypeValidator(JsonNode node) {
        if (!node.isString() && !node.isArray()) {
            throw new IllegalArgumentException();
        }
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
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Value is [%s] but should be [%s]".formatted(nodeType.getName(), types));
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
        return constNode.isEqualTo(node) ? ValidationResult.success() : ValidationResult.failure("Expected " + constNode.toPrintableString());
    }
}

class EnumValidator implements Validator {
    private final List<JsonNode> enumNodes;

    EnumValidator(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.enumNodes = Collections.unmodifiableList(node.asArray());
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (enumNodes.stream().anyMatch(node::isEqualTo)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Expected any of [%s]".formatted(enumNodes.stream().map(JsonNode::toPrintableString).toList()));
        }
    }
}

class MultipleOfValidator implements Validator {
    private final BigDecimal factor;

    MultipleOfValidator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.factor = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return ValidationResult.success();
        }

        if (node.asNumber().remainder(factor).doubleValue() == 0.0) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("%s is not multiple of %s".formatted(node.asNumber(), factor));
        }
    }
}

class MaximumValidator implements Validator {
    private final BigDecimal max;

    MaximumValidator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.max = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return ValidationResult.success();
        }

        if (node.asNumber().compareTo(max) <= 0) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("%s is greater than %s".formatted(node.asNumber(), max));
        }
    }
}

class ExclusiveMaximumValidator implements Validator {
    private final BigDecimal max;

    ExclusiveMaximumValidator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.max = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return ValidationResult.success();
        }

        if (node.asNumber().compareTo(max) < 0) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("%s is greater or equal to %s".formatted(node.asNumber(), max));
        }
    }
}

class MinimumValidator implements Validator {
    private final BigDecimal min;

    MinimumValidator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.min = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return ValidationResult.success();
        }

        if (node.asNumber().compareTo(min) >= 0) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("%s is lesser than %s".formatted(node.asNumber(), min));
        }
    }
}

class ExclusiveMinimumValidator implements Validator {
    private final BigDecimal min;

    ExclusiveMinimumValidator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.min = node.asNumber();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return ValidationResult.success();
        }

        if (node.asNumber().compareTo(min) > 0) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("%s is lesser or equal to %s".formatted(node.asNumber(), min));
        }
    }
}

class MaxLengthValidator implements Validator {
    private final int maxLength;

    MaxLengthValidator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.maxLength = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return ValidationResult.success();
        }

        String string = node.asString();
        if (string.codePointCount(0, string.length()) <= maxLength) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("\"%s\" is longer than %d characters".formatted(string, maxLength));
        }
    }
}

class MinLengthValidator implements Validator {
    private final int minLength;

    MinLengthValidator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.minLength = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return ValidationResult.success();
        }

        String string = node.asString();
        if (string.codePointCount(0, string.length()) >= minLength) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("\"%s\" is shorter than %d characters".formatted(string, minLength));
        }
    }
}

class PatternValidator implements Validator {
    private final Pattern pattern;

    PatternValidator(JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.pattern = Pattern.compile(node.asString());
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return ValidationResult.success();
        }

        if (pattern.matcher(node.asString()).find()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("\"%s\" does not match regular expression [%s]".formatted(node.asString(), pattern.toString()));
        }
    }
}

class MaxItemsValidator implements Validator {
    private final int maxItems;

    MaxItemsValidator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.maxItems = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return ValidationResult.success();
        }

        if (node.asArray().size() <= maxItems) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Array has more than %d items".formatted(maxItems));
        }
    }
}

class MinItemsValidator implements Validator {
    private final int minItems;

    MinItemsValidator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.minItems = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return ValidationResult.success();
        }

        if (node.asArray().size() >= minItems) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Array has less than %d items".formatted(minItems));
        }
    }
}

class UniqueItemsValidator implements Validator {
    private final boolean unique;

    UniqueItemsValidator(JsonNode node) {
        if (!node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.unique = node.asBoolean();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray() || !unique) {
            return ValidationResult.success();
        }

        List<JsonNode> parsed = new ArrayList<>();
        List<JsonNode> jsonNodes = node.asArray();
        for (int i = 0; i < jsonNodes.size(); i++) {
            JsonNode element = jsonNodes.get(i);
            if (parsed.stream().anyMatch(element::isEqualTo)) {
                return ValidationResult.failure("Array contains non-unique item at index [%d]".formatted(i));
            }
            parsed.add(element);
        }
        return ValidationResult.success();
    }
}

class MaxContainsValidator implements Validator {
    private final String containsPath;
    private final int max;

    MaxContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.containsPath = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.CONTAINS))
                .map(ctx::getAbsoluteUri)
                .orElse(null);
        this.max = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray() || containsPath == null) {
            return ValidationResult.success();
        }

        long count = ctx.getAnnotations().stream()
                .filter(a -> a.header().schemaLocation().equals(containsPath))
                .count();
        if (count <= max) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Array contains more than %d matching items".formatted(max));
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class MinContainsValidator implements Validator {
    private final String containsPath;
    private final int min;

    MinContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.containsPath = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.CONTAINS))
                .map(ctx::getAbsoluteUri)
                .orElse(null);
        this.min = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray() || containsPath == null) {
            return ValidationResult.success();
        }

        long count = ctx.getAnnotations().stream()
                .filter(a -> a.header().schemaLocation().equals(containsPath))
                .count();
        if (count >= min) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Array contains less than %d matching items".formatted(min));
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class MaxPropertiesValidator implements Validator {
    private final int max;

    MaxPropertiesValidator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.max = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return ValidationResult.success();
        }

        if (node.asObject().size() <= max) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Object has more than %d properties".formatted(max));
        }
    }
}

class MinPropertiesValidator implements Validator {
    private final int min;

    MinPropertiesValidator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.min = node.asInteger().intValueExact();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return ValidationResult.success();
        }

        if (node.asObject().size() >= min) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Object has less than %d properties".formatted(min));
        }
    }
}

class RequiredValidator implements Validator {
    private final List<String> requiredProperties;

    RequiredValidator(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.requiredProperties = node.asArray().stream().map(JsonNode::asString).toList();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return ValidationResult.success();
        }

        Set<String> keys = node.asObject().keySet();
        if (keys.containsAll(requiredProperties)) {
            return ValidationResult.success();
        } else {
            HashSet<String> unsatisfied = new HashSet<>(requiredProperties);
            unsatisfied.removeAll(keys);
            return ValidationResult.failure("Object does not have some of the required properties [%s]".formatted(unsatisfied));
        }
    }
}

class DependentRequiredValidator implements Validator {
    private final Map<String, List<String>> requiredProperties;

    DependentRequiredValidator(JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.requiredProperties = node.asObject().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toStringList(e.getValue())));
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return ValidationResult.success();
        }

        Set<String> objectKeys = node.asObject().keySet();
        Set<String> requiredSet = objectKeys
                .stream()
                .filter(requiredProperties::containsKey)
                .map(requiredProperties::get)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        if (objectKeys.containsAll(requiredSet)) {
            return ValidationResult.success();
        } else {
            requiredSet.removeAll(objectKeys);
            return ValidationResult.failure("Object does not have some of the required properties [%s]".formatted(requiredSet));
        }
    }

    private List<String> toStringList(JsonNode node) {
        return node.asArray().stream().map(JsonNode::asString).toList();
    }
}