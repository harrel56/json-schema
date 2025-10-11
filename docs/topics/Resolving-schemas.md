# Resolving schemas

Every schema is uniquely identified by its URI. It is common to refer to schema definitions (`$defs`) in the scope of a single schema document
by using JSON pointers (e.g. `#/$defs/foo`). 
But sometimes referencing schemas from different documents is necessary, and this can be achieved by:
1. **Preregistering (preloading) all schema documents that might get referenced**. 
This approach is recommended when the number of referenced schemas is either small or known in advance.
It will also ensure the validity of all registered schemas early on.
```java
validator.registerSchema(uri1, schema1);
validator.registerSchema(uri2, schema2);
validator.registerSchema(uri3, schema3);
```
2. **Implementing [SchemaResolver](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SchemaResolver.html) interface**.
Recommended when schemas need to be loaded dynamically and are not known in advance.
Common use cases would be fetching from the web or from a file system.

**There is no built-in mechanism to fetch schemas from the web** — due to safety reasons, it is discouraged by specification.
The only "external" schemas that will be loaded by default are the official draft meta-schemas.
They are packaged with a jar and are directly read as a classpath resource.

> Each validator instance has its own "registration space," so it's impossible to reference schemas registered in a different validator instance.

> It is possible to override schemas (register multiple times under the same URI), but it is not recommended and this behavior might change in the future.
{style="warning"}

## SchemaResolver interface

```java
public interface SchemaResolver {
    Result resolve(String uri);
}
```

It can be a simple lambda:
```java
SchemaResolver resolver = uriString -> SchemaResolver.Result.fromString("true");
new ValidatorFactory().withSchemaResolver(resolver);
```
Please note this example is oversimplified and will cause every validation against external schema successful.

> Resolver which always returns a result (not `Result.empty()`) will also "resolve" even official meta-schemas,
> making them effectively useless. Note that this behavior might be useful if you purposefully want to adjust official meta-schemas.
{style="warning"}

A special `Result` object needs to be returned from the resolver: it is just a wrapper for a value to allow returning different types.
<deflist>
<def title='Result.empty()'>
Means that the resolver was not able to resolve the schema (or does not support given URI).
Similar to <code>Optional.empty()</code>.
</def>
<def title='Result.fromString(String rawSchema)'>
A result which contains a raw JSON/YAML string representing the schema. 

Example:
```java
SchemaResolver.Result.fromString("{}");
```
</def>
<def title='Result.fromJsonNode(JsonNode schemaNode)'>
A result which contains already parsed instance of <code>dev.harrel.jsonschemaJsonNode</code> representing the schema.

Example:
```java
JsonNode schemaNode = new JacksonNode.Factory().create("{}");
SchemaResolver.Result.fromJsonNode(schemaNode);
```
</def>
<def title='Result.fromProviderNode(Object schemaProviderNode)'>
A result which contains already parsed instance of <a href='Jackson.md#provider-node'>provider node</a> representing the schema.
Please ensure that you are using a correct type according to your <a href='JSON-YAML-providers.md'>JSON/YAML provider</a>.

Example:
```java
Object schemaProviderNode = new ObjectMapper().readTree("{}");
SchemaResolver.Result.fromProviderNode(schemaProviderNode);
```
</def>
</deflist>

## Using multiple resolvers

Let's say you have multiple ways of fetching external schemas. Instead of putting all logic combined into a single resolver,
you can split the logic between multiple resolvers and then combine them:
```java
SchemaResolver resolver = SchemaResolver.compose(ftpResolver, fileResolver, httpResolver);
new ValidatorFactory().withSchemaResolver(resolver);
```
When resolving an external schema, it will:
1. Invoke `ftpResolver`, if `Result.empty()` returned, go to the next step.
2. Invoke `fileResolver`, if `Result.empty()` returned, go to the next step.
3. Invoke `httpResolver`, if `Result.empty()` returned, go to the next step.
4. Invoke the internal resolver which only resolves official meta-schemas. if `Result.empty()` returned, go to the next step.
5. If it was a direct validation call reference, it will throw [SchemaNotFoundException](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SchemaNotFoundException.html).
If it was referenced via `$ref` keyword (or similar), the validation against this keyword will fail.

## Details of ID resolution

Every schema has its own retrieval URI:
1. If parsed via `SchemaResolver` - it's equal to the URI for which the resolution is performed.
2. If registered via `Validator` and registration URI was provided - it's the same as the registration URI.
3. If registered via `Validator` and registration URI was **NOT** provided - a unique retrieval URI is generated in the form of `https://harrel.dev/{randomSeed}`.

> Every schema can always be found under its retrieval URI.
{style="note"}

Additionally, every schema can have its own `$id` (`id` in older drafts) defined. 
This causes the schema to be registered under additional URI:
1. If `$id` is an absolute URI - it's registered as it is.
2. If `$id` is a relative URI - it's resolved against retrieval URI. For example: `https://harrel.dev/12345678` + `/my-schema` -> `https://harrel.dev/my-schema`.
3. If `$id` is a relative URI and registration (retrieval) URI is also relative - first, retrieval URI is resolved against a generated URI, and then the `$id` is resolved against the result. For example: `https://harrel.dev/12345678` + `/schemas/registration` + `/my-schema` -> `https://harrel.dev/schemas/my-schema`.

> Please keep in mind that specification explicitly discourages usage of non-absolute URIs in `$id`s.
{style="warning"}

> Using relative URIs when registering or retrieving schema is also discouraged and only works for backward compatibility reasons.
> Support for such cases might be removed in future versions.
{style="warning"}