package dev.harrel.jsonschema;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.harrel.jsonschema.Vocabulary.VALIDATION_VOCABULARY;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableList;

interface ValidatingEvaluator extends Evaluator {
    @Override
    default Set<String> getVocabularies() {
        return VALIDATION_VOCABULARY;
    }
}

class TypeEvaluator implements ValidatingEvaluator {
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
            List<String> typeNames = unmodifiableList(types.stream().map(SimpleType::getName).collect(Collectors.toList()));
            return Result.failure(String.format("Value is [%s] but should be %s", nodeType.getName(), typeNames));
        }
    }
}

class ConstEvaluator implements ValidatingEvaluator {
    private final JsonNode constNode;

    ConstEvaluator(JsonNode node) {
        this.constNode = node;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        return constNode.isEqualTo(node) ? Result.success() : Result.failure("Expected " + constNode.toPrintableString());
    }
}

class EnumEvaluator implements ValidatingEvaluator {
    private final List<JsonNode> enumNodes;
    private final String failMessage;

    EnumEvaluator(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.enumNodes = unmodifiableList(node.asArray());
        List<String> printList = enumNodes.stream().map(JsonNode::toPrintableString).collect(Collectors.toList());
        this.failMessage = String.format("Expected any of [%s]", printList);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        for (JsonNode enumNode : enumNodes) {
            if (enumNode.isEqualTo(node)) {
                return Result.success();
            }
        }
        return Result.failure(failMessage);
    }
}

class MultipleOfEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("%s is not multiple of %s", node.asNumber(), factor));
        }
    }
}

class MaximumEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("%s is greater than %s", node.asNumber(), max));
        }
    }
}

class ExclusiveMaximumEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("%s is greater or equal to %s", node.asNumber(), max));
        }
    }
}

class MinimumEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("%s is less than %s", node.asNumber(), min));
        }
    }
}

class ExclusiveMinimumEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("%s is less than or equal to %s", node.asNumber(), min));
        }
    }
}

class MaxLengthEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("\"%s\" is longer than %d characters", string, maxLength));
        }
    }
}

class MinLengthEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("\"%s\" is shorter than %d characters", string, minLength));
        }
    }
}

class PatternEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("\"%s\" does not match regular expression [%s]", node.asString(), pattern));
        }
    }
}

class MaxItemsEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("Array has more than %d items", maxItems));
        }
    }
}

class MinItemsEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("Array has less than %d items", minItems));
        }
    }
}

class UniqueItemsEvaluator implements ValidatingEvaluator {
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
                return Result.failure(String.format("Array contains non-unique item at index [%d]", i));
            }
            parsed.add(element);
        }
        return Result.success();
    }
}

class MaxContainsEvaluator implements ValidatingEvaluator {
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

        int containsCount = ctx.getSiblingAnnotation(Keyword.CONTAINS, node.getJsonPointer(), List.class)
                .map(Collection::size)
                .orElse(0);
        if (containsCount <= max) {
            return Result.success();
        } else {
            return Result.failure(String.format("Array contains more than %d matching items", max));
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class MinContainsEvaluator implements ValidatingEvaluator {
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

        int containsCount = ctx.getSiblingAnnotation(Keyword.CONTAINS, node.getJsonPointer(), List.class)
                .map(Collection::size)
                .orElse(Integer.MAX_VALUE);
        if (containsCount >= min) {
            return Result.success();
        } else {
            return Result.failure(String.format("Array contains less than %d matching items", min));
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class MaxPropertiesEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("Object has more than %d properties", max));
        }
    }
}

class MinPropertiesEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("Object has less than %d properties", min));
        }
    }
}

class RequiredEvaluator implements ValidatingEvaluator {
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
            HashSet<String> unsatisfied = new HashSet<>(requiredProperties);
            unsatisfied.removeAll(keys);
            return Result.failure(String.format("Object does not have some of the required properties [%s]", unsatisfied));
        }
    }
}

class DependentRequiredEvaluator implements ValidatingEvaluator {
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
            return Result.failure(String.format("Object does not have some of the required properties [%s]", requiredSet));
        }
    }

    private List<String> toStringList(JsonNode node) {
        return unmodifiableList(node.asArray().stream().map(JsonNode::asString).collect(Collectors.toList()));
    }
}