package dev.harrel.jsonschema;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class TypeEvaluator implements Evaluator {
    private final Set<SimpleType> types;

    TypeEvaluator(JsonNode node) {
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        SimpleType nodeType = node.getNodeType();
        if (types.contains(nodeType) || nodeType == SimpleType.INTEGER && types.contains(SimpleType.NUMBER)) {
            return Result.success();
        } else {
            return Result.failure("Value is [%s] but should be [%s]".formatted(nodeType.getName(), types));
        }
    }
}

class ConstEvaluator implements Evaluator {
    private final JsonNode constNode;

    ConstEvaluator(JsonNode node) {
        this.constNode = node;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        return constNode.isEqualTo(node) ? Result.success() : Result.failure("Expected " + constNode.toPrintableString());
    }
}

class EnumEvaluator implements Evaluator {
    private final List<JsonNode> enumNodes;

    EnumEvaluator(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.enumNodes = Collections.unmodifiableList(node.asArray());
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (enumNodes.stream().anyMatch(node::isEqualTo)) {
            return Result.success();
        } else {
            return Result.failure("Expected any of [%s]".formatted(enumNodes.stream().map(JsonNode::toPrintableString).toList()));
        }
    }
}

class MultipleOfEvaluator implements Evaluator {
    private final BigDecimal factor;

    MultipleOfEvaluator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.factor = node.asNumber();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class MaximumEvaluator implements Evaluator {
    private final BigDecimal max;

    MaximumEvaluator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.max = node.asNumber();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class ExclusiveMaximumEvaluator implements Evaluator {
    private final BigDecimal max;

    ExclusiveMaximumEvaluator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.max = node.asNumber();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class MinimumEvaluator implements Evaluator {
    private final BigDecimal min;

    MinimumEvaluator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.min = node.asNumber();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class ExclusiveMinimumEvaluator implements Evaluator {
    private final BigDecimal min;

    ExclusiveMinimumEvaluator(JsonNode node) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException();
        }
        this.min = node.asNumber();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class MaxLengthEvaluator implements Evaluator {
    private final int maxLength;

    MaxLengthEvaluator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.maxLength = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class MinLengthEvaluator implements Evaluator {
    private final int minLength;

    MinLengthEvaluator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.minLength = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class PatternEvaluator implements Evaluator {
    private final Pattern pattern;

    PatternEvaluator(JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.pattern = Pattern.compile(node.asString());
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class MaxItemsEvaluator implements Evaluator {
    private final int maxItems;

    MaxItemsEvaluator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.maxItems = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class MinItemsEvaluator implements Evaluator {
    private final int minItems;

    MinItemsEvaluator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.minItems = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
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

class UniqueItemsEvaluator implements Evaluator {
    private final boolean unique;

    UniqueItemsEvaluator(JsonNode node) {
        if (!node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.unique = node.asBoolean();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray() || !unique) {
            return Result.success();
        }

        List<JsonNode> parsed = new ArrayList<>();
        List<JsonNode> jsonNodes = node.asArray();
        for (int i = 0; i < jsonNodes.size(); i++) {
            JsonNode element = jsonNodes.get(i);
            if (parsed.stream().anyMatch(element::isEqualTo)) {
                return Result.failure("Array contains non-unique item at index [%d]".formatted(i));
            }
            parsed.add(element);
        }
        return Result.success();
    }
}

class MaxContainsEvaluator implements Evaluator {
    private final String containsPath;
    private final int max;

    MaxContainsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.containsPath = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.CONTAINS))
                .map(ctx::getAbsoluteUri)
                .orElse(null);
        this.max = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray() || containsPath == null) {
            return Result.success();
        }

        long count = ctx.getAnnotations().stream()
                .filter(a -> a.header().schemaLocation().equals(containsPath))
                .count();
        if (count <= max) {
            return Result.success();
        } else {
            return Result.failure("Array contains more than %d matching items".formatted(max));
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class MinContainsEvaluator implements Evaluator {
    private final String containsPath;
    private final int min;

    MinContainsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.containsPath = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.CONTAINS))
                .map(ctx::getAbsoluteUri)
                .orElse(null);
        this.min = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray() || containsPath == null) {
            return Result.success();
        }

        long count = ctx.getAnnotations().stream()
                .filter(a -> a.header().schemaLocation().equals(containsPath))
                .count();
        if (count >= min) {
            return Result.success();
        } else {
            return Result.failure("Array contains less than %d matching items".formatted(min));
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class MaxPropertiesEvaluator implements Evaluator {
    private final int max;

    MaxPropertiesEvaluator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.max = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        if (node.asObject().size() <= max) {
            return Result.success();
        } else {
            return Result.failure("Object has more than %d properties".formatted(max));
        }
    }
}

class MinPropertiesEvaluator implements Evaluator {
    private final int min;

    MinPropertiesEvaluator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.min = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        if (node.asObject().size() >= min) {
            return Result.success();
        } else {
            return Result.failure("Object has less than %d properties".formatted(min));
        }
    }
}

class RequiredEvaluator implements Evaluator {
    private final List<String> requiredProperties;

    RequiredEvaluator(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.requiredProperties = node.asArray().stream().map(JsonNode::asString).toList();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Set<String> keys = node.asObject().keySet();
        if (keys.containsAll(requiredProperties)) {
            return Result.success();
        } else {
            HashSet<String> unsatisfied = new HashSet<>(requiredProperties);
            unsatisfied.removeAll(keys);
            return Result.failure("Object does not have some of the required properties [%s]".formatted(unsatisfied));
        }
    }
}

class DependentRequiredEvaluator implements Evaluator {
    private final Map<String, List<String>> requiredProperties;

    DependentRequiredEvaluator(JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.requiredProperties = node.asObject().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toStringList(e.getValue())));
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Set<String> objectKeys = node.asObject().keySet();
        Set<String> requiredSet = objectKeys
                .stream()
                .filter(requiredProperties::containsKey)
                .map(requiredProperties::get)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        if (objectKeys.containsAll(requiredSet)) {
            return Result.success();
        } else {
            requiredSet.removeAll(objectKeys);
            return Result.failure("Object does not have some of the required properties [%s]".formatted(requiredSet));
        }
    }

    private List<String> toStringList(JsonNode node) {
        return node.asArray().stream().map(JsonNode::asString).toList();
    }
}