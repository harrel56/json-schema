package dev.harrel.jsonschema;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.harrel.jsonschema.providers.InternalProviderUtil.canUseNativeEquals;
import static java.util.Collections.*;

class TypeEvaluator implements Evaluator {
    private final Set<SimpleType> types;

    TypeEvaluator(JsonNode node) {
        if (!node.isString() && !node.isArray()) {
            throw new IllegalArgumentException();
        }
        if (node.isString()) {
            this.types = singleton(SimpleType.fromName(node.asString()));
        } else {
            this.types = node.asArray().stream()
                    .map(JsonNode::asString)
                    .map(SimpleType::fromName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        SimpleType nodeType = node.getNodeType();
        if (types.contains(nodeType) || nodeType == SimpleType.INTEGER && types.contains(SimpleType.NUMBER)) {
            return Result.success();
        } else {
            return Result.failure(() -> {
                List<String> typeNames = types.stream().map(SimpleType::getName).collect(Collectors.toList());
                return String.format("Value is [%s] but should be %s", nodeType.getName(), typeNames);
            });
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
        boolean valid = canUseNativeEquals(constNode) && canUseNativeEquals(node) ? constNode.equals(node) : JsonNodeUtil.equals(constNode, node);
        return valid ? Result.success() : Result.failure("Expected " + constNode.toPrintableString());
    }
}

class EnumEvaluator implements Evaluator {
    private final Set<JsonNode> enumNodes;
    private final String failMessage;
    private final boolean canUseNativeEquals;

    EnumEvaluator(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.enumNodes = unmodifiableSet(new LinkedHashSet<>(node.asArray()));
        List<String> printList = enumNodes.stream().map(JsonNode::toPrintableString).collect(Collectors.toList());
        this.failMessage = String.format("Expected any of [%s]", printList);
        this.canUseNativeEquals = canUseNativeEquals(node);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (canUseNativeEquals && canUseNativeEquals(node)) {
            return enumNodes.contains(node) ? Result.success() : Result.failure(failMessage);
        } else {
            for (JsonNode enumNode : enumNodes) {
                if (JsonNodeUtil.equals(enumNode, node)) {
                    return Result.success();
                }
            }
            return Result.failure(failMessage);
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
            return Result.failure(() -> String.format("%s is not multiple of %s", node.asNumber(), factor));
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
            return Result.failure(() -> String.format("%s is greater than %s", node.asNumber(), max));
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
            return Result.failure(() -> String.format("%s is greater or equal to %s", node.asNumber(), max));
        }
    }
}

class LegacyMaximumEvaluator implements Evaluator {
    private final Evaluator delegate;

    LegacyMaximumEvaluator(SchemaParsingContext ctx, JsonNode node) {
        JsonNode exclusiveNode = ctx.getCurrentSchemaObject().get(Keyword.EXCLUSIVE_MAXIMUM);
        if (exclusiveNode != null && exclusiveNode.isBoolean() && exclusiveNode.asBoolean()) {
            this.delegate = new ExclusiveMaximumEvaluator(node);
        } else {
            this.delegate = new MaximumEvaluator(node);
        }
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        return delegate.evaluate(ctx, node);
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
            return Result.failure(() -> String.format("%s is less than %s", node.asNumber(), min));
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
            return Result.failure(() -> String.format("%s is less than or equal to %s", node.asNumber(), min));
        }
    }
}

class LegacyMinimumEvaluator implements Evaluator {
    private final Evaluator delegate;

    LegacyMinimumEvaluator(SchemaParsingContext ctx, JsonNode node) {
        JsonNode exclusiveNode = ctx.getCurrentSchemaObject().get(Keyword.EXCLUSIVE_MINIMUM);
        if (exclusiveNode != null && exclusiveNode.isBoolean() && exclusiveNode.asBoolean()) {
            this.delegate = new ExclusiveMinimumEvaluator(node);
        } else {
            this.delegate = new MinimumEvaluator(node);
        }
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        return delegate.evaluate(ctx, node);
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
            return Result.failure(() -> String.format("\"%s\" is longer than %d characters", string, maxLength));
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
            return Result.failure(() -> String.format("\"%s\" is shorter than %d characters", string, minLength));
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
            return Result.failure(() -> String.format("\"%s\" does not match regular expression [%s]", node.asString(), pattern));
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
            return Result.failure(() -> String.format("Array has more than %d items", maxItems));
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
            return Result.failure(() -> String.format("Array has less than %d items", minItems));
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

        List<JsonNode> jsonNodes = node.asArray();
        if (canUseNativeEquals(node)) {
            Set<JsonNode> parsed = new HashSet<>();
            for (int i = 0; i < jsonNodes.size(); i++) {
                if (!parsed.add(jsonNodes.get(i))) {
                    return Result.failure(String.format("Array contains non-unique item at index [%d]", i));
                }
            }
        } else {
            List<JsonNode> parsed = new ArrayList<>(jsonNodes.size());
            for (int i = 0; i < jsonNodes.size(); i++) {
                JsonNode element = jsonNodes.get(i);
                if (parsed.stream().anyMatch(parsedNode -> JsonNodeUtil.equals(parsedNode, element))) {
                    return Result.failure(String.format("Array contains non-unique item at index [%d]", i));
                }
                parsed.add(element);
            }
        }

        return Result.success();
    }
}

class MaxContainsEvaluator implements Evaluator {
    private final int max;

    MaxContainsEvaluator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.max = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        Object containsAnnotation = ctx.getSiblingAnnotation(Keyword.CONTAINS);
        int containsCount = containsAnnotation instanceof Collection ? ((Collection<?>) containsAnnotation).size() : 0;
        if (containsCount <= max) {
            return Result.success();
        } else {
            return Result.failure(() -> String.format("Array contains more than %d matching items", max));
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class MinContainsEvaluator implements Evaluator {
    private final int min;

    MinContainsEvaluator(JsonNode node) {
        if (!node.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.min = node.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        Object containsAnnotation = ctx.getSiblingAnnotation(Keyword.CONTAINS);
        int containsCount = containsAnnotation instanceof Collection ? ((Collection<?>) containsAnnotation).size() : Integer.MAX_VALUE;
        if (containsCount >= min) {
            return Result.success();
        } else {
            return Result.failure(() -> String.format("Array contains less than %d matching items", min));
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
            return Result.failure(() -> String.format("Object has more than %d properties", max));
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
            return Result.failure(() -> String.format("Object has less than %d properties", min));
        }
    }
}

class RequiredEvaluator implements Evaluator {
    private final List<String> requiredProperties;

    RequiredEvaluator(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.requiredProperties = unmodifiableList(node.asArray().stream().map(JsonNode::asString).collect(Collectors.toList()));
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
            Supplier<String> messageSupplier = () -> {
                HashSet<String> unsatisfied = new HashSet<>(requiredProperties);
                unsatisfied.removeAll(keys);
                return String.format("Object does not have some of the required properties [%s]", unsatisfied);
            };
            return Result.failure(messageSupplier);
        }
    }
}

class DependenciesLegacyEvaluator implements Evaluator {
    private final DependentRequiredEvaluator requiredDelegate;
    private final DependentSchemasEvaluator schemasDelegate;

    DependenciesLegacyEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }

        Map<Boolean, Map<String, JsonNode>> splitMap = node.asObject().entrySet().stream()
                .collect(Collectors.partitioningBy(entry -> entry.getValue().isArray(),
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        this.requiredDelegate = new DependentRequiredEvaluator(splitMap.get(true));
        this.schemasDelegate = new DependentSchemasEvaluator(ctx, splitMap.get(false));
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        Result result = requiredDelegate.evaluate(ctx, node);
        if (!result.isValid()) {
            return result;
        } else {
            return schemasDelegate.evaluate(ctx, node);
        }
    }
}

class DependentRequiredEvaluator implements Evaluator {
    private final Map<String, List<String>> requiredProperties;

    DependentRequiredEvaluator(JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.requiredProperties = toMap(node.asObject());
    }

    DependentRequiredEvaluator(Map<String, JsonNode> objectNode) {
        this.requiredProperties = toMap(objectNode);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Set<String> requiredSet = new HashSet<>();
        Set<String> objectKeys = node.asObject().keySet();
        for (String objectKey : objectKeys) {
            List<String> keys = requiredProperties.get(objectKey);
            if (keys != null) {
                requiredSet.addAll(keys);
            }
        }
        if (objectKeys.containsAll(requiredSet)) {
            return Result.success();
        } else {
            return Result.failure(() -> {
                requiredSet.removeAll(objectKeys);
                return String.format("Object does not have some of the required properties [%s]", requiredSet);
            });
        }
    }

    private static Map<String, List<String>> toMap(Map<String, JsonNode> objectNode) {
        return objectNode.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toStringList(e.getValue())));
    }

    private static List<String> toStringList(JsonNode node) {
        return unmodifiableList(node.asArray().stream().map(JsonNode::asString).collect(Collectors.toList()));
    }
}