# JSON.org

## Required dependency

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20250107</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'org.json:json:20250107'
```

</tab>
</tabs>

The newest version is always supported.

> The oldest supported version is `20201115` (inclusive).
{style="warning"}

Adapter classes are:
- [OrgJsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/OrgJsonNode.html),
- [OrgJsonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/OrgJsonNode.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class form JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> JSON.org doesn't have one provider node class. It is represented with `Object` and can be one of:
> - `org.json.JSONObject`,
> - `org.json.JSONArray`,
> - `org.json.JSONObject.NULL`,
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
JsonNodeFactory factory = new OrgJsonNode.Factory();
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Converting String to JsonNode

```java
JsonNodeFactory factory = new OrgJsonNode.Factory();
JsonNode jsonNode = factory.create("{}");
```

### Converting provider node to JsonNode

```java
Object providerNode = new JSONTokener("{}").nextValue();
JsonNodeFactory factory = new OrgJsonNode.Factory();
JsonNode jsonNode = factory.wrap(providerNode);
```
### Using Validator with provider nodes

```java
Object providerSchemaNode = new JSONTokener("{}").nextValue();
URI schemaUri = validator.registerSchema(providerSchemaNode);

Object providerInstanceNode = new JSONTokener("true").nextValue();
Validator.Result result = validator.validate(schemaUri, providerInstanceNode);
```