# maximumSize

It is basically a merge of two other keywords: `maxProperties` and `maxItems`.

## Evaluator implementation
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

## Configuration
```java
EvaluatorFactory factory = new EvaluatorFactory.Builder()
        .withKeyword("maximumSize", (node) -> new MaximumSizeEvaluator(node))
        .build();
Validator validator = new ValidatorFactory().withEvaluatorFactory(factory).createValidator();
```

## Schema JSON
```json
{
  "maximumSize": 2
}
```
