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

> To follow the pattern of official keywords,
> it is recommended to simply succeed if an evaluator does not support given type.
> For example, `maxLength` applies only to strings and will always succeed for non-string values.
{style="note"}

Evaluation can result in either:
- success (`Evaluator.Result.success()`),
- success with an annotation (`Evaluator.Result.success(Object annotation)`),
- failure (`Evaluator.Result.failure()`),
- failure with a message (`Evaluator.Result.failure(String message)`).
- failure with an [internationalized message](Internationalization.md) (`Evaluator.Result.formattedFailure(String key, Object... args)`).

