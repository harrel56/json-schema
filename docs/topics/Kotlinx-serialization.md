# Kotlinx.serialization

## Required dependency

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-serialization-json</artifactId>
    <version>1.8.1</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1'
```

</tab>
</tabs>

The newest version is always supported.

> The oldest supported version is `1.0.0` (inclusive).
{style="warning"}

Adapter classes are:
- [KotlinxJsonFactory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/KotlinxJsonNode.html),
- [KotlinxJsonFactory.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/KotlinxJsonNode.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class from JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> Kotlinx.serialization's provider node is `kotlinx.serialization.json.JsonElement` class.
{style="note"}

## Usage

### Creating Validator instance

```kotlin
val factory: JsonNodeFactory = KotlinxJsonNode.Factory()
val validator: Validator = ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator()
```

### Using custom Json configuration

> While it is possible to use custom `Json` configuration,
> it is not recommended as it may lead to non-spec behavior.
{style="warning"}

```kotlin
val json = Json { isLenient = true }
val factory: JsonNodeFactory = KotlinxJsonNode.Factory(json)
val validator: Validator = ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator()
```

### Converting String to JsonNode

```kotlin
val factory: JsonNodeFactory = KotlinxJsonNode.Factory()
val jsonNode: JsonNode = factory.create("{}")
```

### Converting provider node to JsonNode

```kotlin
val providerNode: kotlinx.serialization.json.JsonElement = Json.parseToJsonElement("{}")
val factory: JsonNodeFactory = KotlinxJsonNode.Factory()
val jsonNode: JsonNode = factory.wrap("{}")
```

### Using Validator with provider nodes

```kotlin
val providerSchemaNode: kotlinx.serialization.json.JsonElement = Json.parseToJsonElement("{}")
val schemaUri: URI = validator.registerSchema(providerSchemaNode)

val providerInstanceNode: kotlinx.serialization.json.JsonElement = Json.parseToJsonElement("true")
val result: Validator.Result = validator.validate(schemaUri, providerInstanceNode)
```