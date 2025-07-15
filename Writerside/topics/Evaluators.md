# Evaluators

[Evaluator](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Evaluator.html) represents keyword logic.
Each keyword can have **at most one** evaluator attached.

```java
public interface Evaluator {
    Result evaluate(EvaluationContext ctx, JsonNode node);
    default int getOrder() {
        return 0;
    }
}
```

> Evaluator instantiation logic should be contained in [evaluator factories](Evaluator-factories.md).

> Generally, in the scope of a single schema, the order in which the evaluators will be called is not specified.
> This is due to the JSON specification that states the order of properties is not significant
> and thus different JSON libraries may produce a different order.
> Sometimes we want to call evaluator before/after another one,
> and this can be achieved by overriding the default `getOrder()` method.

## Evaluation context

It can be used to trigger another schema validation process in several ways:
- **Internal** - in most cases, you would want to use this one.
If a schema you want to validate against is contained somewhere in the keyword's value, this is the correct approach (e.g. `properties` or `anyOf` keywords use it). 
When using this approach, it should be guaranteed that the reference resolves to a valid schema.
It **won't** trigger any schema resolvers.
- **`$ref` like** - follows the same behavior as `$ref` keyword.
- **`$dynamicRef` like** (since *draft 2020-12*) - follows the same behavior as `$dynamicRef` keyword.
- **`$recursiveRef` like** (available only in *draft 2019-09*) - follows the same behavior as `$recursiveRef` keyword.

## Result object

> Evaluation logic **must not** return null.
{style="warning"}

> Evaluation logic **should not** throw any exceptions under normal circumstances. Such exceptions are not caught and will be propagated to the user code,
> effectively failing the whole validation process.
{style="warning"}

Evaluation can result in either:
- success (`Evaluator.Result.success()`),
- success with an annotation (`Evaluator.Result.success(Object annotation)`),
- failure (`Evaluator.Result.failure()`),
- failure with a message (`Evaluator.Result.failure(String message)`).

## Examples

> To follow the pattern of official keywords,
> it is recommended to simply succeed if an evaluator does not support given type.
> For example, `maxLength` applies only to strings and will always succeed for non-string values.
{style="note"}

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