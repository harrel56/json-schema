# lenientType

Alternative to the traditional `type` keyword which allows type coercion.
It can be defined in multiple ways, but in this example rules are simple:
- any primitive type can be coerced to a string,
- only strings can be coerced to another type (let's not consider `1` being treated as `true` and similar).

## Evaluator implementation
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

## Configuration
```java
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
        .withKeyword("lenientType", LenientTypeEvaluator::new)
        .build();
Validator validator = new ValidatorFactory().withEvaluatorFactory(factory).createValidator();
```

## Schema JSON
```json
{
  "lenientType": ["object", "number"]
}
```