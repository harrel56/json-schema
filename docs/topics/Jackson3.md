# Jackson 3.x

## Required dependencies

> This adapter code is provided as a standalone artifact.

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>dev.harrel.json.providers</groupId>
    <artifactId>jackson3</artifactId>
    <version>%latest_version%</version>
</dependency>
<dependency>
    <groupId>tools.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>3.0.2</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'dev.harrel.json.providers:jackson3:%latest_version%'
implementation 'tools.jackson.core:jackson-databind:3.0.2'
```

</tab>
</tabs>

The newest version is always supported.

> The oldest supported version is `3.0.0` (inclusive).
{style="warning"}

Adapter classes are:
 - [Jackson3Node](https://javadoc.io/doc/dev.harrel.json.providers/jackson3/latest/dev/harrel/json/providers/jackson3/Jackson3Node.html),
 - [Jackson3Node.Factory](https://javadoc.io/doc/dev.harrel.json.providers/jackson3/latest/dev/harrel/json/providers/jackson3/Jackson3Node.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class from JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> Jackson's (3.x) provider node is `tools.jackson.databind.JsonNode` class.
{style="note"}

## Usage

### Creating Validator instance

```java
JsonNodeFactory factory = new Jackson3Node.Factory();
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Using custom ObjectMapper

> While it is possible to use custom ObjectMapper,
> it is not recommended as its different configurations may lead to non-spec behavior.
{style="warning"}

```java
ObjectMapper objectMapper = new ObjectMapper();
JsonNodeFactory factory = new Jackson3Node.Factory(objectMapper);
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Converting String to JsonNode

```java
JsonNodeFactory factory = new Jackson3Node.Factory();
JsonNode jsonNode = factory.create("{}");
```

### Converting provider node to JsonNode

```java
tools.jackson.databind.JsonNode providerNode = new ObjectMapper().readTree("{}");
JsonNodeFactory factory = new Jackson3Node.Factory();
JsonNode jsonNode = factory.wrap(providerNode);
```
### Using Validator with provider nodes

```java
tools.jackson.databind.JsonNode providerSchemaNode = new ObjectMapper().readTree("{}");
URI schemaUri = validator.registerSchema(providerSchemaNode);

tools.jackson.databind.JsonNode providerInstanceNode = new ObjectMapper().readTree("true");
Validator.Result result = validator.validate(schemaUri, providerInstanceNode);
```