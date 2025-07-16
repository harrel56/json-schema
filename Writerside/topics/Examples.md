# Examples

### maximumSize

It is basically a merge of two other keywords: `maxProperties` and `maxItems`.

```json
{
  "maximumSize": 2
}
```

```java
import dev.harrel.jsonschema.EvaluationContext;
import dev.harrel.jsonschema.Evaluator;
import dev.harrel.jsonschema.JsonNode;

class MaximumSizeEvaluator implements Evaluator {
    private final int maxSize;

    MaximumSizeEvaluator(int maxSize) {
        this.maxSize = maxSize;
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

### propertyWithShortestName

It applies a specific schema to a property (or properties) with the shortest name.
It also produces a list of validated properties as an annotation.

> As this keyword works like an applicator, providing an error message is not strictly required.
> It will still contain errors from the applied schema.

```json
{
  "propertyWithShortestName": {
    "type": "string"
  }
}
```

```java
import dev.harrel.jsonschema.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PropertyWithShortestNameEvaluator implements Evaluator {
    private final String schemaRef;

    PropertyWithShortestNameEvaluator(SchemaParsingContext ctx, JsonNode schemaNode) {
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