# Custom dialects

## Dialect registry

Each validator object contains an internal dialect registry which is used to recognize dialect based on `$schema` keyword value.
By default, all official currently supported JSON Schema specification dialects are present in the registry.
The registry can be updated:
```java
new ValidatorFactory().withDialect(new CustomDialect);
```

> Calling `withDialect` method is basically an equivalent of:
> ```java
> dialectRegistry.put(dialect.getMetaSchema(), dialect);
> ```

> Returning `null` from `getMetaSchema()` is **NOT** recommended, and it will prevent it from being registered.
{style=warning}

Please note that overriding existing dialects in the registry is also possible.
To do so, create a dialect implementation which returns the same value from `getMetaSchema()` as the one you want to override.

> Removing dialects from registry is not supported.

## Default dialect

If schema does not contain a `$schema` keyword, a default dialect will be used.
By default, the default dialect is `Draft 2020-12`. You can change it by calling:
```java
new ValidatorFactory().withDefaultDialect(new CustomDialect);
```

> JSON Schema specification does not clearly define what should be done if `$schema` keyword is missing,
> so relaying on that behavior is generally not recommended.
> Other implementations might even forbid such configurations.
{style=warning}

## Dialect types:

1. semi dialect: only metaschema: cannot change evaluator factory,
must @schema to full dialect to resolve spec version. Mostly for simple vocab changes
2. defined dialect without metaschema: no custom meta validation, static vocabs
3. full dialect with metaschema: can be recursive

Registering dialect must have schema URI !!!

## What happens when dialect cannot be resolved?

## examples
 
### Format factory with vocab support

### Another case with vocabs

maybe custom keyword, how to check for vocabs in evaluator factory (package access currently :().
And activating it by using custom metaschema

### Overwrite draft2020-12

### Dialect without vocab hassle

### 

