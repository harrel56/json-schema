# Evaluators

Keyword logic is represented by [Evaluator](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Evaluator.html) interface.
Each keyword can have **at most one** evaluator attached.

```java
public interface Evaluator {
    Result evaluate(EvaluationContext ctx, JsonNode node);
}
```

### EvaluationContext

It can be used to trigger another schema validation process in several ways:
- **Internal** - in most cases, you would want to use this one.
If a schema you want to validate against is contained somewhere in the keyword's value, this is the correct approach (e.g. `properties` or `anyOf` keywords use it). 
When using this approach, it should be guaranteed that the reference resolves to a valid schema.
It **won't** trigger any schema resolvers.
- **`$ref` like** - follows the same behavior as `$ref` keyword.
- **`$dynamicRef` like** (since *draft 2020-12*) - follows the same behavior as `$dynamicRef` keyword.
- **`$recursiveRef` like** (available only in *draft 2019-09*) - follows the same behavior as `$recursiveRef` keyword.