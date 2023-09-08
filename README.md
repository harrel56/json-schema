# json-schema

[![build](https://github.com/harrel56/json-schema/actions/workflows/build.yml/badge.svg)](https://github.com/harrel56/json-schema/actions/workflows/build.yml)
[![maven](https://maven-badges.herokuapp.com/maven-central/dev.harrel/json-schema/badge.svg)](https://mvnrepository.com/artifact/dev.harrel/json-schema)
[![javadoc](https://javadoc.io/badge2/dev.harrel/json-schema/javadoc.svg)](https://javadoc.io/doc/dev.harrel/json-schema)
[![coverage](https://harrel56.github.io/json-schema/jacoco.svg)](https://github.com/harrel56/json-schema/actions/workflows/build.yml)

Java library implementing [JSON schema specification](https://json-schema.org/specification.html):
- compatible with Java 8,
- support for the newest specification draft (*2020-12*) [![Supported spec](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie-json-schema.github.io%2Fbowtie%2Fbadges%2Fjava-json-schema%2Fsupported_versions.json)](https://bowtie-json-schema.github.io/bowtie/) [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie-json-schema.github.io%2Fbowtie%2Fbadges%2Fjava-json-schema%2Fcompliance%2FDraft_2020-12.json)](https://bowtie-json-schema.github.io/bowtie/),
- support for custom keywords,
- support for annotation collection,
- multiple JSON providers to choose from ([supported JSON libraries](#json-providers))
- and no additional dependencies on top of that.

## Demo
You can check out how it works [here](https://harrel.dev/json-schema).
## Installation
Please note that you will also need to include at least one of the supported JSON provider libraries (see [JSON provider setup](#json-providers)).
### Maven
```xml
<dependency>
    <groupId>dev.harrel</groupId>
    <artifactId>json-schema</artifactId>
    <version>1.3.0</version>
</dependency>
```
### Gradle
```groovy
implementation 'dev.harrel:json-schema:1.3.0'
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

## Limitations
Features that are not supported yet:
- ~~`$vocabulary` keyword - all vocabularies' related semantics are not yet there.~~ Vocabularies are supported from version 1.2.0 - [see more](#custom-dialects).
- `format` keyword - the specification doesn't require `format` to perform any validations. Support for official format validation might be added in future versions. Meanwhile, the implementation could be provided by user (see [adding custom keywords](#adding-custom-keywords)).

## JSON providers
Supported providers:
- `com.fasterxml.jackson.core:jackson-databind` (default),
- `com.google.code.gson:gson`,
- `jakarta.json:jakarta.json-api`,
- `org.json:json`,
- `new.minidev:json-smart`,
- `org.codehouse.jettison:jettison`,
- `org.apache.tapestry:tapestry-json` - planned,
- `javax.json:javax.json-api` - planned.

The default provider is `com.fasterxml.jackson.core:jackson-databind`, so if you are not planning on changing the `ValidatorFactory` configuration, **you need to have this dependency present in your project**.

Specific version of provider dependencies which were tested can be found in project POM (uploaded to maven central) listed as optional dependencies.

All adapter classes for JSON provider libs can be found in this [package](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/package-summary.html). Anyone is free to add new adapter classes for any JSON lib of their choice, but keep in mind that it is not trivial. If you do so, ensure that test suites for providers pass.

### Changing JSON provider

| Provider                                    | Tested version                                   | Factory class                                | Provider node class                                                                                                                                      |
|---------------------------------------------|--------------------------------------------------|----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| com.fasterxml.jackson.core:jackson-databind | 2.15.2                                           | dev.harrel.providers.JacksonNode.Factory     | com.fasterxml.jackson.databind.JsonNode                                                                                                                  |
| com.google.code.gson:gson                   | 2.10.1                                           | dev.harrel.providers.GsonNode.Factory        | com.google.gson.JsonElement                                                                                                                              |
| jakarta.json:jakarta.json-api               | 2.1.2 *(with org.eclipse.parsson:parsson:1.1.2)* | dev.harrel.providers.JakartaJsonNode.Factory | jakarta.json.JsonValue                                                                                                                                   |
| org.json:json                               | 20230227                                         | dev.harrel.providers.OrgJsonNode.Factory     | <ul><li>org.json.JSONObject,</li><li>org.json.JSONArray,</li><li>[literal types](#provider-literal-types).</li></ul>                                     |
| new.minidev:json-smart                      | 2.4.11                                           | dev.harrel.providers.JsonSmartNode.Factory   | <ul><li>net.minidev.json.JSONObject,</li><li>net.minidev.json.JSONArray,</li><li>[literal types](#provider-literal-types).</li></ul>                     |
| org.codehouse.jettison:jettison             | 1.5.4                                            | dev.harrel.providers.JettisonNode.Factory    | <ul><li>org.codehaus.jettison.json.JSONObject,</li><li>org.codehaus.jettison.json.JSONArray,</li><li>[literal types](#provider-literal-types).</li></ul> |

#### com.fasterxml.jackson.core:jackson-databind
```java
new ValidatorFactory().withJsonNodeFactory(new JacksonNode.Factory());
```

#### com.google.code.gson:gson
```java
new ValidatorFactory().withJsonNodeFactory(new GsonNode.Factory());
```
#### jakarta.json:jakarta.json-api
Keep in mind that this library contains only interfaces without concrete implementation.
It would be required to also have e.g. `org.glassfish:jakarta.json` dependency in your classpath.
Although, it was tested with newest `jakarta.json-api` version, it should be compatible down to `1.1` version.
```java
new ValidatorFactory().withJsonNodeFactory(new JakartaJsonNode.Factory());
```

#### org.json:json
```java
new ValidatorFactory().withJsonNodeFactory(new OrgJsonNode.Factory());
```

#### new.minidev:json-smart
```java
new ValidatorFactory().withJsonNodeFactory(new JsonSmartNode.Factory());
```

#### org.codehouse.jettison:jettison
```java
new ValidatorFactory().withJsonNodeFactory(new JettisonNode.Factory());
```

### Provider literal types
Some providers don't have a single wrapper class for their JSON node representation:
- `org.json:json`,
- `new.minidev:json-smart`,
- `org.codehouse.jettison:jettison`,

and they represent literal nodes with these classes:
- `java.lang.String`,
- `java.lang.Boolean`,
- `java.lang.Character`,
- `java.lang.Enum`,
- `java.lang.Integer`,
- `java.lang.Long`,
- `java.lang.Double`,
- `java.math.BigInteger`,
- `java.math.BigDecimal`.

## Advanced configuration
### Resolving external schemas
By default, the only schemas that are resolved externally, are specification meta-schemas (e.g. *https://json-schema.org/draft/2020-12/schema*) which are used for validating schemas during registration process. The meta-schema files are fetched from the classpath and are packaged with jar.

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

### Dialects
By default, [draft 2020-12 dialect](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Dialects.Draft2020Dialect.html) is used,
but it can be changed with:
```java
new ValidatorFactory().withDialect(new Dialects.Draft2020Dialect()); // or any other dialect
```
Custom dialects are also supported, see more [here](#custom-dialects).

### Meta-schemas
Dialects come with their meta-schemas. Each schema will be validated by meta-schema provided by used *dialect*.
If validation fails [InvalidSchemaException](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/InvalidSchemaException.html) is thrown.

For each specific schema this behaviour can be overridden by providing *$schema* keyword with desired meta-schema URI. Resolution of meta-schema follows the same [rules](#resolving-external-schemas) as for a regular schema.

There is a configuration option that disables all schema validations (affects *$schema* and vocabularies semantics too):
```java
new ValidatorFactory().withDisabledSchemaValidation(true);
```

### Adding custom keywords
Customizing specific keywords behaviour can be achieved by providing custom [EvaluatorFactory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/EvaluatorFactory.html) implementation.
Each dialect comes with its core *EvaluatorFactory* which will always be used, but additional *EvaluatorFactory* implementation can be provided on top of that.
If you want to completely alter how schemas are validated, please refer to [custom dialects](#custom-dialects).

This example shows an implementation that adds `customKeyword` keyword handling which fails validation if JSON node is not an empty array:
```java
class CustomEvaluatorFactory implements EvaluatorFactory {
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
        return Optional.empty();
    }
}
```

Then it just needs to be attached to `ValidatorFactory`:
```java
new ValidatorFactory().withEvaluatorFactory(new CustomEvaluatorFactory());
```

### Custom dialects
If you want you could provide your custom dialect configuration:
```java
Dialect customDialect = new Dialect() {
    @Override
    public SpecificationVersion getSpecificationVersion() {
        return SpecificationVersion.DRAFT2020_12;
    }
    
    @Override
    public String getMetaSchema() {
        return "https://example.com/custom/schema";
    }
    
    @Override
    public EvaluatorFactory getEvaluatorFactory() {
        return new Draft2020EvaluatorFactory();
    }
    
    @Override
    public Set<String> getSupportedVocabularies() {
        return Collections.singleton("custom-vocabulary");
    }
    
    @Override
    public Set<String> getRequiredVocabularies() {
        return Collections.emptySet();
    }
    
    @Override
    public Map<String, Boolean> getDefaultVocabularyObject() {
        return Collections.singletonMap("custom-vocabulary", true);
    }
};
new ValidatorFactory().withDialect(customDialect);
```
See the [documentation](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Dialect.html) for more details.

