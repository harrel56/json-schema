# JSON/YAML providers

There is an abstraction layer above JSON/YAML structure that allows usage with different JSON libraries.
There are just two interfaces in this layer:
 - [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html),
 - [JsonNodeFactory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNodeFactory.html).

There are many adapter/provider classes for most common JSON/YAML libraries, all of them can be found in the subpages
or in this [package](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/package-summary.html).

## Custom providers

> If the library you are using is not supported by default, you can always implement your own provider.
> The interfaces are not complicated,
> but be aware that getting the implementation right (specification and performance wise) is not as trivial as it may seem.
> It is recommended to test your custom provider against test suites located in this library's repository.
> For more information please check the guide of [implementing your own providers](Custom-JSON-YAML-providers.md).

## Using multiple providers

Generally, mixing up providers is not recommended,
but it may make sense when you want to use different formats (JSON, YAML) for different things (schema, instance parsing).
There is a special API for such a use case:
```java
new ValidatorFactory().withJsonNodeFactories(schemaNodeFactory, instanceNodeFactory);
```