# json-schema

[![build](https://github.com/harrel56/json-schema/actions/workflows/build.yml/badge.svg)](https://github.com/harrel56/json-schema/actions/workflows/build.yml)
[![javadoc](https://javadoc.io/badge2/dev.harrel/json-schema/javadoc.svg)](https://javadoc.io/doc/dev.harrel/json-schema)

Java library implementing [JSON schema specification](https://json-schema.org/specification.html):
- compatible with Java 8,
- support for the newest specification draft (*2020-12*) [![Supported spec](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie-json-schema.github.io%2Fbowtie%2Fbadges%2Fjava-json-schema%2Fsupported_versions.json)](https://bowtie-json-schema.github.io/bowtie/) [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie-json-schema.github.io%2Fbowtie%2Fbadges%2Fjava-json-schema%2Fcompliance%2FDraft_2020-12.json)](https://bowtie-json-schema.github.io/bowtie/),
- support for custom keywords,
- support for annotation collection,
- multiple JSON providers to choose from ([supported JSON libraries](#json-providers))
- and no additional dependencies on top of that.

## Installation
Please note that you will also need to include at least one of the supported JSON provider libraries (see [JSON provider setup](#json-providers)).
### Maven
```xml
<dependency>
    <groupId>dev.harrel</groupId>
    <artifactId>json-schema</artifactId>
    <version>1.0.0</version>
</dependency>
```
### Gradle
```groovy
implementation 'dev.harrel:json-schema:1.0.0'
```

## Usage
To validate JSON against a schema, you just need to invoke:
```java
String schema = """
        {
          "type": "boolean"
        }""";
String instance = "true";
boolean valid = new ValidatorFactory().validate(schema, instance).isValid();
```
Validation result could be queried for more verbose output than a simple boolean flag:
```java
Validator.Result result = new ValidatorFactory().validate(schema, instance);
boolean valid = result.isValid(); // Boolean flag indicating if validation succeeded
List<Error> errors = result.getErrors(); // Details where validation exactly failed
List<Annotation> annotations = result.getAnnotations(); // Collected annotation during validation process
```
`Error` and `Annotation` classes contain specific information where the event occurred, along with error message or annotation value. For specific structure details please refer to the [documentation](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/ResultItem.html).

### Reusing schema
Probably most common case is to validate multiple JSON objects against one specific schema. Approach listed above parses schema for each validation request. To avoid this performance hit, it is better to use [Validator](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Validator.html) class directly.
```java
Validator validator = new ValidatorFactory().createValidator();
URI schemaUri = validator.registerSchema(schema); // Returns URI which should be used to refer to this schema
Validator.Result result1 = validator.validate(schemaUri, instance1);
Validator.Result result2 = validator.validate(schemaUri, instance2);
```
This way, schema is parsed only once. You could also register multiple schemas this way and refer to them independently. Keep in mind that the "registration space" for schemas is common for one `Validator` - this can be used to refer dynamically between schemas.

## <a name="json-providers"></a> JSON providers
Supported providers:
- `com.fasterxml.jackson.core:jackson-databind` (default),
- `com.google.code.gson:gson`,
- `new.minidev:json-smart` - planned,
- `org.json:json` - planned,
- `org.apache.tapestry:tapestry-json` - planned,
- `org.codehouse.jettison:jettison` - planned,
- `jakarta.json:jakarta.json-api` - planned,
- `javax.json:javax.json-api` - planned.

The default provider is `com.fasterxml.jackson.core:jackson-databind`, so if you are not planning on changing the `ValidatorFactory` configuration, **you need to have this dependency present in your project**.

Specific version of provider dependencies which were tested can be found in project POM (uploaded to maven central) listed as optional dependencies.

All adapter classes for JSON provider libs can be found in this [package](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/package-summary.html). Anyone is free to add new adapter classes for any JSON lib of their choice, but keep in mind that it is not trivial. If you do so, ensure that test suites for providers pass.

### Changing JSON provider

| Provider                                    | Tested version | Factory class                            | Provider node class                     |
|---------------------------------------------|----------------|------------------------------------------|-----------------------------------------|
| com.fasterxml.jackson.core:jackson-databind | 2.15.1+        | dev.harrel.providers.JacksonNode.Factory | com.fasterxml.jackson.databind.JsonNode |
| com.google.code.gson:gson                   | 2.10.1+        | dev.harrel.providers.GsonNode.Factory    | com.google.gson.JsonElement             |

#### com.fasterxml.jackson.core:jackson-databind
```java
new ValidatorFactory().withJsonNodeFactory(new JacksonNode.Factory());
```

#### com.google.code.gson:gson
```java
new ValidatorFactory().withJsonNodeFactory(new GsonNode.Factory());
```

## Advanced configuration
### <a name="schema-resolver"></a> Resolving external schemas
By default, the only schema that is resolved externally, is specification meta-schema for *draft 2020-12* which is used for validating schemas during registration process. The meta-schema file is fetched from the classpath and is packaged with jar.

**There is no mechanism to pull schemas via HTTP requests**. If such behaviour is required it should be implemented by the user.

Providing custom `SchemaResolver` would look like this:
```java
SchemaResolver resolver = (String uri) -> {
    if ("urn:my-schema1".equals(uri)) {
        // Here goes the logic to retrieve this schema
        // This may be e.g. HTTP call
        String rawSchema = ...
        return SchemaResolver.Result.fromString(rawSchema);
    } else if ("urn:my-schema2".equals(uri)) {
        // Same thing here
        String rawSchema = ...
        return SchemaResolver.Result.fromString(rawSchema);
    } else {
        return SchemaResolver.Result.empty();
    }
};
```
Then it just needs to be attached to `ValidatorFactory`:
```java
new ValidatorFactory().withSchemaResolver(resolver);
```
For more information about return type please refer to the [documentation](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SchemaResolver.Result.html).

### Default meta-schema
By default, upon registration of each schema, it gets validated against meta-schema (*https://json-schema.org/draft/2020-12/schema*). If validation fails [InvalidSchemaException](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/InvalidSchemaException.html) is thrown.

For each specific schema this behaviour can be overridden by providing *$schema* keyword with desired meta-schema URI. Resolution of meta-schema follows the same [rules](#schema-resolver) as for a regular schema.

If you want to change default meta-schema, configure `ValidatorVactory` like this:
```java
new ValidatorFactory().withDefaultMetaSchema("your-meta-schema-uri");
```

If you don't want to validate schemas by default, set default meta-schema to null:
```java
new ValidatorFactory().withDefaultMetaSchema(null);
```

### Adding custom keywords
Customizing specific keywords behaviour can be achieved by providing custom [EvaluatorFactory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/EvaluatorFactory.html) implementation.

If you only want to add additional keywords on top of those supported in *draft 2020-12*, please extend [Draft2020EvaluatorFactory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Draft2020EvaluatorFactory.html) class.

This example shows an implementation that adds `customKeyword` keyword handling which fails validation if JSON node is not an empty array:
```java
class CustomEvaluatorFactory extends Draft2020EvaluatorFactory {
    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        if ("customKeyword".equals(fieldName)) {
            return Optional.of((evaluationContext, instanceNode) -> {
                if (instanceNode.isArray() && instanceNode.asArray().isEmpty()) {
                    return Evaluator.Result.success(); // Optionally, you could also pass annotation
                } else {
                    return Evaluator.Result.failure(); // Optionally, you could also pass error message
                }
            });
        }
        return super.create(ctx, fieldName, node);
    }
}
```

Then it just needs to be attached to `ValidatorFactory`:
```java
new ValidatorFactory().withEvaluatorFactory(new CustomEvaluatorFactory());
```