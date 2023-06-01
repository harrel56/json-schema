# json-schema

[![build](https://github.com/harrel56/json-schema/actions/workflows/build.yml/badge.svg)](https://github.com/harrel56/json-schema/actions/workflows/build.yml)
[![javadoc](https://javadoc.io/badge2/dev.harrel/json-schema/javadoc.svg)](https://javadoc.io/doc/dev.harrel/json-schema)

Java library implementing [JSON schema specification](https://json-schema.org/specification.html):
- compatible with Java 8,
- support for the newest specification draft (*2020-12*),
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
### Resolving external schemas
By default, the only schema that is resolved externally, is specification meta-schema for *draft 2020-12* which is used for validating schemas during registration process. The meta-schema file is fetched from the classpath and is packaged with jar.

Certainly, **there is no mechanism to pull schemas via HTTP requests**. If such behaviour is required it should be implemented by the user.

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

### Adding custom keywords