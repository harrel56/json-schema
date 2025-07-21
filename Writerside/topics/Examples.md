# Examples

### maximumSize

It is basically a merge of two other keywords: `maxProperties` and `maxItems`.

Evaluator implementation:
```java
import dev.harrel.jsonschema.EvaluationContext;
import dev.harrel.jsonschema.Evaluator;
import dev.harrel.jsonschema.JsonNode;

class MaximumSizeEvaluator implements Evaluator {
    private final int maxSize;

    MaximumSizeEvaluator(JsonNode schemaNode) {
        if (!schemaNode.isInteger()) {
            throw new IllegalArgumentException();
        }
        this.maxSize = schemaNode.asInteger().intValueExact();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (node.isObject() && node.asObject().size() > maxSize) {
            return Result.failure("Object should contain at most %d properties".formatted(maxSize));
        } else if (node.isArray() && node.asArray().size() > maxSize) {
            return Result.failure("Array should contain at most %d items".formatted(maxSize));
        } else {
            return Result.success();
        }
    }
}
```

Configuration:
```java
EvaluatorFactory factory = new EvaluatorFactory.Builder()
        .withKeyword("maximumSize", (node) -> new MaximumSizeEvaluator(node))
        .build();
Validator validator = new ValidatorFactory().withEvaluatorFactory(factory).createValidator();
```

Schema JSON:
```json
{
  "maximumSize": 2
}
```

### propertyWithShortestName

It applies a specific schema to a property (or properties) with the shortest name.
It also produces a list of validated properties as an annotation.

> As this keyword works like an applicator, providing an error message is not strictly required.
> It will still contain errors from the applied schema.

Evaluator implementation:
```java
import dev.harrel.jsonschema.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PropertyWithShortestNameEvaluator implements Evaluator {
    private final String schemaRef;

    PropertyWithShortestNameEvaluator(SchemaParsingContext ctx, JsonNode schemaNode) {
        if (!schemaNode.isBoolean && !schemaNode.isObject()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getAbsoluteUri(schemaNode);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success(List.of());
        }

        int min = Integer.MAX_VALUE;
        List<Map.Entry<String, JsonNode>> propertiesToValidate = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            int length = entry.getKey().length();
            if (min > length) {
                min = length;
                propertiesToValidate = new ArrayList<>();
            }
            if (min == length) {
                propertiesToValidate.add(entry);
            }
        }

        for (Map.Entry<String, JsonNode> entry : propertiesToValidate) {
            if (!ctx.resolveInternalRefAndValidate(schemaRef, entry.getValue())) {
                return Result.failure();
            }
        }
        List<String> propNames = propertiesToValidate.stream().map(Map.Entry::getKey).toList();
        return Result.success(propNames);
    }
}
```

Configuration:
```java
EvaluatorFactory factory = new EvaluatorFactory.Builder()
        .withKeyword("propertyWithShortestName", (ctx, node) -> new PropertyWithShortestNameEvaluator(ctx, node))
        .build();
Validator validator = new ValidatorFactory().withEvaluatorFactory(factory).createValidator();
```

Schema JSON:
```json
{
  "propertyWithShortestName": {
    "type": "string"
  }
}
```

### lenientType

Alternative to the traditional `type` keyword which allows type coercion. 
It can be defined in multiple ways, but in this example rules are simple:
- any primitive type can be coerced to a string,
- only strings can be coerced to another type (let's not consider `1` being treated as `true` and similar).

Evaluator implementation:
```java
import dev.harrel.jsonschema.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

class LenientTypeEvaluator implements Evaluator {
    private final Set<SimpleType> types;

    LenientTypeEvaluator(JsonNode node) {
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
        if (nodeType != SimpleType.ARRAY && nodeType != SimpleType.OBJECT && types.contains(SimpleType.STRING)) {
            return Result.success();
        }
        if (types.contains(nodeType) || nodeType == SimpleType.INTEGER && types.contains(SimpleType.NUMBER)) {
            return Result.success();
        } else if (nodeType == SimpleType.STRING) {
            for (SimpleType type : types) {
                if (canConvert(node.asString(), type)) {
                    return Result.success();
                }
            }
        }

        List<String> typeNames = types.stream().map(SimpleType::getName).toList();
        return Result.failure(String.format("Value is [%s] but should be %s", nodeType.getName(), typeNames));
    }

    private boolean canConvert(String value, SimpleType type) {
        return switch (type) {
            case NULL -> value.equals("null");
            case BOOLEAN -> value.equals("true") || value.equals("false");
            case STRING -> true;
            case INTEGER -> {
                try {
                    new BigInteger(value);
                    yield true;
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case NUMBER -> {
                try {
                    new BigDecimal(value);
                    yield true;
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case ARRAY, OBJECT -> false;
        };
    }
}
```

Configuration:
```java
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
        .withKeyword("lenientType", LenientTypeEvaluator::new)
        .build();
Validator validator = new ValidatorFactory().withEvaluatorFactory(factory).createValidator();
```

Schema JSON:
```json
{
  "lenientType": ["object", "number"]
}
```