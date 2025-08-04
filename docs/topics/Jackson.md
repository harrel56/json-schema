# Jackson

## Required dependency

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.19.0</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'com.fasterxml.jackson.core:jackson-databind:2.19.0'
```

</tab>
</tabs>

The newest version is always supported.

> The oldest supported version is `2.2.0` (inclusive).
{style="warning"}

Adapter classes are:
 - [JacksonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JacksonNode.html),
 - [JacksonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JacksonNode.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class from JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> Jackson's provider node is `com.fasterxml.jackson.databind.JsonNode` class.
{style="note"}

## Usage

### Creating Validator instance

The default factory is `JacksonNode.Factory`, so setting it explicitly is not required.

```java
JsonNodeFactory factory = new JacksonNode.Factory();
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
JsonNodeFactory factory = new JacksonNode.Factory(objectMapper);
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Converting String to JsonNode

```java
JsonNodeFactory factory = new JacksonNode.Factory();
JsonNode jsonNode = factory.create("{}");
```

### Converting provider node to JsonNode

```java
com.fasterxml.jackson.databind.JsonNode providerNode = new ObjectMapper().readTree("{}");
JsonNodeFactory factory = new JacksonNode.Factory();
JsonNode jsonNode = factory.wrap(providerNode);
```
### Using Validator with provider nodes

```java
com.fasterxml.jackson.databind.JsonNode providerSchemaNode = new ObjectMapper().readTree("{}");
URI schemaUri = validator.registerSchema(providerSchemaNode);

com.fasterxml.jackson.databind.JsonNode providerInstanceNode = new ObjectMapper().readTree("true");
Validator.Result result = validator.validate(schemaUri, providerInstanceNode);
```