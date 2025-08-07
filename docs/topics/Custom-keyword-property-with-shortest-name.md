# propertyWithShortestName

It applies a specific schema to a property (or properties) with the shortest name.
It also produces a list of validated properties as an annotation.

> As this keyword works like an applicator, providing an error message is not strictly required.
> It will still contain errors from the applied schema.

## Evaluator implementation
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

## Configuration
```java
EvaluatorFactory factory = new EvaluatorFactory.Builder()
        .withKeyword("propertyWithShortestName", (ctx, node) -> new PropertyWithShortestNameEvaluator(ctx, node))
        .build();
Validator validator = new ValidatorFactory().withEvaluatorFactory(factory).createValidator();
```

## Schema JSON
```json
{
  "propertyWithShortestName": {
    "type": "string"
  }
}
```