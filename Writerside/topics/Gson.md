# Gson

## Required dependency

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.13.1</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'com.google.code.gson:gson:2.13.1'
```

</tab>
</tabs>

The newest version is always supported.

> The oldest supported version is `2.3` (inclusive).
{style="warning"}

Adapter classes are:
- [GsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/GsonNode.html),
- [GsonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/GsonNode.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class form JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> Gson's provider node is `com.google.gson.JsonElement` class.
{style="note"}

## Usage

### Creating Validator instance

```java
JsonNodeFactory factory = new GsonNode.Factory();
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Converting String to JsonNode

```java
JsonNodeFactory factory = new GsonNode.Factory();
JsonNode jsonNode = factory.create("{}");
```

### Converting provider node to JsonNode

```java
com.google.gson.JsonElement providerNode = JsonParser.parseString("{}");
JsonNodeFactory factory = new GsonNode.Factory();
JsonNode jsonNode = factory.wrap(providerNode);
```
### Using Validator with provider nodes

```java
com.google.gson.JsonElement providerSchemaNode = JsonParser.parseString("{}");
URI schemaUri = validator.registerSchema(providerSchemaNode);

com.google.gson.JsonElement providerInstanceNode = JsonParser.parseString("true");
Validator.Result result = validator.validate(schemaUri, providerInstanceNode);
```