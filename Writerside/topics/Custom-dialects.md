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

## Dialect types

Depending on your needs, you might want to define only specific parts of a dialect.

### Meta-schema only

This is the only official way to extend/create dialects, and it's implementation-agnostic.

**You will need:**
1. The actual meta-schema: 
    - It can be provided either by `SchemaResolver` or registered directly in `Validator` instance.
    - It has to finally resolve to an actual concrete dialect (from dialect registry).
    It cannot be recursive, either directly or indirectly - it may reference (by `$schema`) another "meta-schema only" dialect,
    but eventually the chain of references must point to a dialect from the dialect registry.

**It allows you:**
1. Changing active vocabularies (by `$vocabulary` keyword). You can, for example, disable all validation keywords.
2. Defining custom validation for your schemas.

**You will not be able to:**
1. Define custom keywords, as the logic cannot be expressed directly in JSON.
2. Make the dialect independent of another one. As mentioned before: it eventually needs to point to a registered dialect.
3. Change the supported, required and default vocabularies.

### Dialect without meta-schema

This is not really recommended approach as such a dialect can only be used as a default dialect.

**You will need:**
1. The actual implementation of `Dialect`.

**It allows you (only for schemas lacking `$schema` keyword):**
1. Defining custom keywords via `EvaluatorFactory`.
2. Changing the supported, required and default vocabularies.

**You will not be able to:**
1. Affect



### bop

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

