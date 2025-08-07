# JSON smart

## Required dependency

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>net.minidev</groupId>
    <artifactId>json-smart</artifactId>
    <version>2.5.2</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'net.minidev:json-smart:2.5.2'
```

</tab>
</tabs>

The newest version is always supported.

> The oldest supported version is `1.1` (inclusive).
{style="warning"}

Adapter classes are:
- [JsonSmartNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JsonSmartNode.html),
- [JsonSmartNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JsonSmartNode.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class from JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> JSON smart doesn't have one provider node class. It is represented with `Object` and can be one of:
> - `net.minidev.json.JSONObject`,
> - `net.minidev.json.JSONArray`,
> - `java.lang.String`,
> - `java.lang.Boolean`,
> - `java.lang.Character`,
> - `java.lang.Enum`,
> - `java.lang.Integer`,
> - `java.lang.Long`,
> - `java.lang.Double`,
> - `java.math.BigInteger`,
> - `java.math.BigDecimal`,
> - `null`.
>
{style="note"}

## Usage

### Creating Validator instance

```java
JsonNodeFactory factory = new JsonSmartNode.Factory();
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Using custom JSONParser

> While it is possible to use custom JSONParser,
> it is not recommended as its different configurations may lead to non-spec behavior.
{style="warning"}

```java
JSONParser jsonParser = new JSONParser(MODE_JSON_SIMPLE);
JsonNodeFactory factory = new JsonSmartNode.Factory(jsonParser);
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Converting String to JsonNode

```java
JsonNodeFactory factory = new JsonSmartNode.Factory();
JsonNode jsonNode = factory.create("{}");
```

### Converting provider node to JsonNode

```java
Object providerNode = new JSONTokener("{}").nextValue();
JsonNodeFactory factory = new JsonSmartNode.Factory();
JsonNode jsonNode = factory.wrap(providerNode);
```
### Using Validator with provider nodes

```java
Object providerSchemaNode = new JSONParser(MODE_JSON_SIMPLE).parse("{}");
URI schemaUri = validator.registerSchema(providerSchemaNode);

Object providerInstanceNode = new JSONParser(MODE_JSON_SIMPLE).parse("true");
Validator.Result result = validator.validate(schemaUri, providerInstanceNode);
```