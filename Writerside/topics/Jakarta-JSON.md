# Jakarta JSON

## Required dependencies

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>jakarta.json</groupId>
    <artifactId>jakarta.json-api</artifactId>
    <version>2.1.3</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'jakarta.json:jakarta.json-api:2.1.3'
```

</tab>
</tabs>

This library contains only API without concrete implementation.
It is required to add also some library that implements this API.
[Parsson](https://github.com/eclipse-ee4j/parsson) is a reference implementation, and it is used in tests:

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>org.eclipse.parsson</groupId>
    <artifactId>parsson</artifactId>
    <version>1.1.7</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'org.eclipse.parsson:parsson:1.1.7'
```

</tab>
</tabs>

Adapter classes are:
- [JakartaJsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JakartaJsonNode.html),
- [JakartaJsonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JakartaJsonNode.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class form JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> Jakarta JSON's provider node is `jakarta.json.JsonValue` class.
{style="note"}

## Usage

### Creating Validator instance

```java
JsonNodeFactory factory = new JakartaJsonNode.Factory();
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Using custom JsonParserFactory

> While it is possible to use custom JsonParserFactory,
> it is not recommended as its different configurations may lead to non-spec behavior.
{style="warning"}

```java
JsonParserFactory jsonParserFactory = Json.createParserFactory(Map.of());
JsonNodeFactory factory = new JakartaJsonNode.Factory(jsonParserFactory);
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Converting String to JsonNode

```java
JsonNodeFactory factory = new JakartaJsonNode.Factory();
JsonNode jsonNode = factory.create("{}");
```

### Converting provider node to JsonNode

```java
try (JsonReader reader = Json.createReader(new StringReader("{}"))) {
    jakarta.json.JsonValue providerNode = reader.readValue();
    JsonNodeFactory factory = new JakartaJsonNode.Factory();
    JsonNode jsonNode = factory.wrap(providerNode);
}
```
### Using Validator with provider nodes

```java
jakarta.json.JsonValue providerSchemaNode, providerInstanceNode;
try (JsonReader reader = Json.createReader(new StringReader("{}"))) {
    providerSchemaNode = reader.readValue();
}
try (JsonReader reader = Json.createReader(new StringReader("true"))) {
    providerInstanceNode = reader.readValue();
}
URI schemaUri = validator.registerSchema(providerSchemaNode);
Validator.Result result = validator.validate(schemaUri, providerInstanceNode);
```