# Evaluator factories

[EvaluatorFactory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/EvaluatorFactory.html) is responsible for the creation of evaluators.
It is used during a schema parsing process.

```java
public interface EvaluatorFactory {
    Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode);
}
```

The `create` method is called for every JSON property-value pair present in a schema.
Sometimes it may be surprising because not all JSON objects might seem like schemas.
Consider this example:

```json
{
  "properties": {
    "maxLength": false
  }  
}
```

The `create` method will be called twice:
1. for pair `properties` - `object`
2. and for pair `maxLength` - `boolean`.

As `maxLength` keyword only makes sense with numeric values, this may seem a bit odd,
but such scenarios are commonly found in meta-schemas. 
Please remember to handle such cases properly in your custom keywords as well.

## Evaluator factory builder

Most of the time implementing factory from scratch is not necessary.
It is recommended to use builder instead. It allows binding a specific keyword to an evaluator provider.

To create such a binding, call:
```java
EvaluatorFactory factory = new EvaluatorFactory.Builder()
        /* In rare cases when the value of the keyword is not important */
        .withKeyword("customKeyword", () -> new CustomEvaluator())
        
        /* When value of the keyword is needed */
        .withKeyword("anotherKeyword", (node) -> new AnotherEvaluator(node))
        
        /* When context is needed as well, mostly for applicators */
        .withKeyword("thirdKeyword", (ctx, node) -> new ThirdEvaluator(ctx, node))
        .build();
```

> While registration of the same keyword multiple times works (latest registration overrides), there is no way to unregister a keyword.

> There is also another feature in place to allow for supporting only specific types of a keyword value.
> If the evaluator provider throws any exception, it will be caught and the evaluator just won't get registered.
> This may also be used in other ways, depending on your use case.
> Very common example would look like this:
> ```java
> class MaxLengthEvaluator implements Evaluator {
>     private final int maxLength;
> 
>     MaxLengthEvaluator(JsonNode node) {
>         // This keyword only makes sense if it has a numeric value
>         // Let's throw if it is not the case, preventing registration
>         if (!node.isInteger()) {
>             throw new IllegalArgumentException();
>         }
>         this.maxLength = node.asInteger().intValueExact();
>     }
> 
>     @Override
>     public Result evaluate(EvaluationContext ctx, JsonNode node) {
>         // skipped for brevity
>     }
> }
> ```

## Schema parsing

> Schema JSON is parsed depth first.

Validator might contain multiple evaluator factories, and they complement each other.
If one factory does not support given keyword, the next one in order is used.

Ordering and combining multiple factories into one factory are done by:
```java
EvaluatorFactory rootFactory = EvaluatorFactory.compose(factory1, factory2, factory3);
```

Let's consider an example with a keyword `x` which is supported only by `factory3`:
1. `factory1` returns empty optional.
2. `factory2` returns empty optional.
3. `factory3` returns an actual evaluator.

What if keyword `y` is supported by both `factory2` and `factory3`?
1. `factory1` returns empty optional.
2. `factory2` returns an actual evaluator.
3. `factory3` **is never called as evaluator was already provided.**

> This is important when setting additional evaluator factory for validator:
> ```java
> new ValidatorFactory().withEvaluatorFactory(factory);
> ```
> 
> As there already is a dialect-specific evaluator factory, they need to be combined.
> It is done so the custom factory goes first:
> ```java
> EvaluatorFactory.compose(customFactory, dialectFactory);
> ```
> 
> This way, the custom factory takes precedence which allows for overriding keywords which would be provided by dialect-specific factory.
