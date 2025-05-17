# Jettison

## Required dependency

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>org.codehaus.jettison</groupId>
    <artifactId>jettison</artifactId>
    <version>1.5.4</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'org.codehaus.jettison:jettison:1.5.4'
```

</tab>
</tabs>

The newest version is always supported.

> The oldest supported version is `1.5.0` (inclusive).
{style="warning"}

Adapter classes are:
- [JettisonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JettisonNode.html),
- [JettisonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JettisonNode.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class from JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> Jettison doesn't have one provider node class. It is represented with `Object` and can be one of:
> - `org.codehaus.jettison.json.JSONObject`,
> - `org.codehaus.jettison.json.JSONArray`,
> - `org.codehaus.jettison.json.JSONObject.NULL`,
> - `org.codehaus.jettison.json.JSONObject.EXPLICIT_NULL`,
> - `java.lang.String`,
> - `java.lang.Boolean`,
> - `java.lang.Character`,
> - `java.lang.Enum`,
> - `java.lang.Integer`,
> - `java.lang.Long`,
> - `java.lang.Double`,
> - `java.math.BigInteger`,
> - `java.math.BigDecimal`.
>
{style="note"}

## Usage

### Creating Validator instance

```java
JsonNodeFactory factory = new JettisonNode.Factory();
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Converting String to JsonNode

```java
JsonNodeFactory factory = new JettisonNode.Factory();
JsonNode jsonNode = factory.create("{}");
```

### Converting provider node to JsonNode

```java
Object providerNode = new JSONTokener("{}").nextValue();
JsonNodeFactory factory = new JettisonNode.Factory();
JsonNode jsonNode = factory.wrap(providerNode);
```
### Using Validator with provider nodes

```java
Object providerSchemaNode = new JSONTokener("{}").nextValue();
URI schemaUri = validator.registerSchema(providerSchemaNode);

Object providerInstanceNode = new JSONTokener("true").nextValue();
Validator.Result result = validator.validate(schemaUri, providerInstanceNode);
```