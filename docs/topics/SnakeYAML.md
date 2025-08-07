# SnakeYAML

## Required dependency

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>2.4</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'org.yaml:snakeyaml:2.4'
```

</tab>
</tabs>

The newest version is always supported.

> The oldest supported version is `1.26` (inclusive).
{style="warning"}

Adapter classes are:
- [SnakeYamlNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/SnakeYamlNode.html),
- [SnakeYamlNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/SnakeYamlNode.Factory.html).

## Provider node

The [JsonNode](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/JsonNode.html) abstraction wraps a concrete class from JSON/YAML library,
which is called a "provider node."
It is possible to interact with the API using provider nodes directly,
but there is no type information at compile time, so please ensure that you are using the correct class.

> SnakeYAML's provider node is `org.yaml.snakeyaml.nodes.Node` class.
{style="note"}

## Constraints

Library is compatible with YAML 1.1 standard. However, there are few constraints:
- all object keys are treated as strings,
- object keys cannot be duplicated,
- anchors and aliases are supported while override syntax (`<<`) is not.

## Usage

### Creating Validator instance

```java
JsonNodeFactory factory = new SnakeYamlNode.Factory();
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Using custom Yaml configuration

> While it is possible to use custom Yaml configuration,
> it is not recommended as it may lead to non-spec behavior.
{style="warning"}

```java
Yaml yaml = new Yaml();
JsonNodeFactory factory = new SnakeYamlNode.Factory(yaml);
Validator validator = new ValidatorFactory()
        .withJsonNodeFactory(factory)
        .createValidator();
```

### Converting String to JsonNode

```java
JsonNodeFactory factory = new SnakeYamlNode.Factory();
JsonNode jsonNode = factory.create("{}");
```

### Converting provider node to JsonNode

```java
org.yaml.snakeyaml.nodes.Node providerNode = new Yaml().compose(new StringReader("{}"));
JsonNodeFactory factory = new SnakeYamlNode.Factory();
JsonNode jsonNode = factory.wrap(providerNode);
```
### Using Validator with provider nodes

```java
org.yaml.snakeyaml.nodes.Node providerSchemaNode = new Yaml().compose(new StringReader("{}"));
URI schemaUri = validator.registerSchema(providerSchemaNode);

org.yaml.snakeyaml.nodes.Node providerInstanceNode = new Yaml().compose(new StringReader("true"));
Validator.Result result = validator.validate(schemaUri, providerInstanceNode);
```