# json-schema

[![build](https://github.com/harrel56/json-schema/actions/workflows/tests.yml/badge.svg)](https://github.com/harrel56/json-schema/actions/workflows/tests.yml)
[![maven](https://maven-badges.herokuapp.com/maven-central/dev.harrel/json-schema/badge.svg)](https://mvnrepository.com/artifact/dev.harrel/json-schema)
[![javadoc](https://javadoc.io/badge2/dev.harrel/json-schema/javadoc.svg)](https://javadoc.io/doc/dev.harrel/json-schema)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=harrel56_json-schema&metric=coverage)](https://sonarcloud.io/summary/new_code?id=harrel56_json-schema)
[![Size](https://jarhell.harrel.dev/api/v1/badges/total_size/dev.harrel:json-schema)](https://jarhell.harrel.dev/packages/dev.harrel:json-schema)
[![Bytecode](https://jarhell.harrel.dev/api/v1/badges/effective_bytecode/dev.harrel:json-schema)](https://jarhell.harrel.dev/packages/dev.harrel:json-schema)
[![Dependencies](https://jarhell.harrel.dev/api/v1/badges/dependencies/dev.harrel:json-schema)](https://jarhell.harrel.dev/packages/dev.harrel:json-schema)

Java library implementing [JSON schema specification](https://json-schema.org/specification.html):
- compatible with Java 8,
- support for all recent [specification versions](#dialects) [![Supported spec](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie-json-schema.github.io%2Fbowtie%2Fbadges%2Fjava-dev.harrel.json-schema%2Fsupported_versions.json&color=blue)](https://bowtie.report/#/implementations/java-json-schema):
  - Draft 2020-12 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft2020-12.json&color=brightgreen)](https://bowtie.report/#/dialects/draft2020-12),
  - Draft 2019-09 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft2019-09.json&color=brightgreen)](https://bowtie.report/#/dialects/draft2019-09),
  - Draft 07 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft7.json&color=brightgreen)](https://bowtie.report/#/dialects/draft7),
  - Draft 06 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft6.json&color=brightgreen)](https://bowtie.report/#/dialects/draft6),
  - Draft 04 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft4.json&color=brightgreen)](https://bowtie.report/#/dialects/draft4),
- support for [custom keywords](#adding-custom-keywords),
- support for annotation collection,
- support for [format validation](#format-validation) (for a price of one additional dependency 😉),
- compatible with most of the JSON/YAML libraries ([supported libraries](#jsonyaml-providers)),
- and no additional dependencies on top of that.

Check how it compares with other implementations:
- [Bowtie](https://bowtie.report) - specification compliance (only mandatory behavior),
- [Creek's benchmark](https://www.creekservice.org/json-schema-validation-comparison/performance) - benchmark for JVM based implementations.

## Demo
You can check out how it works [here](https://harrel.dev/json-schema).
## Installation
Please note that you will also need to include at least one of the supported JSON provider libraries (see [JSON provider setup](#jsonyaml-providers)).
### Maven
```xml
<dependency>
    <groupId>dev.harrel</groupId>
    <artifactId>json-schema</artifactId>
    <version>1.8.1</version>
</dependency>
```
### Gradle
```groovy
implementation 'dev.harrel:json-schema:1.8.1'
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
`Error` and `Annotation` classes contain specific information where the event occurred, along with error message or annotation value. For specific structure details please refer to the [documentation](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Error.html).

### Reusing schema
Probably most common case is to validate multiple JSON objects against one specific schema. Approach listed above parses schema for each validation request. To avoid this performance hit, it is better to use [Validator](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Validator.html) class directly.
```java
Validator validator = new ValidatorFactory().createValidator();
URI schemaUri = validator.registerSchema(schema); // Returns URI which should be used to refer to this schema
Validator.Result result1 = validator.validate(schemaUri, instance1);
Validator.Result result2 = validator.validate(schemaUri, instance2);
```
This way, schema is parsed only once. You could also register multiple schemas this way and refer to them independently. Keep in mind that the "registration space" for schemas is common for one `Validator` - this can be used to refer dynamically between schemas.

### Thread safety
- `ValidatorFactory` **IS NOT** thread-safe as it contains mutable configuration elements which may lead to memory visibility issues.
`validate(...)` methods are however stateless, so if the factory is configured before it has been shared between threads, it can be used concurrently.
- `Validator` **IS** thread-safe as its configuration is immutable. The internal schema registry is configured for multi-threaded usage.
- All library provided implementations (`SchemaResolver`, `JsonNodeFactory`, `EvaluatorFactory`, `Evaluator`) are thread safe.
For custom user implementations: if intended for use in multi-threaded environment, the implementation should ensure thread safety.

## JSON/YAML providers

The default provider is `com.fasterxml.jackson.core:jackson-databind`, so if you are not planning on changing the `ValidatorFactory` configuration, **you need to have this dependency present in your project**.

| Provider                                                  | Oldest supported version      | Factory class                                                                                                                                | Provider node class                                                                                                                                      |
|-----------------------------------------------------------|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **com.fasterxml.jackson.core:jackson-databind (default)** | `2.2.0`                       | [JacksonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JacksonNode.Factory.html)         | com.fasterxml.jackson.databind.JsonNode                                                                                                                  |
| com.google.code.gson:gson                                 | `2.3`                         | [GsonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/GsonNode.Factory.html)               | com.google.gson.JsonElement                                                                                                                              |
| jakarta.json:jakarta.json-api                             | `2.0.1` (tested with parsson) | [JakartaJsonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JakartaJsonNode.Factory.html) | jakarta.json.JsonValue                                                                                                                                   |
| org.jetbrains.kotlinx:kotlinx-serialization-json          | `1.0.0`                       | [KotlinxJsonFactory.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/KotlinxJson.Factory.html)  | kotlinx.serialization.json.JsonElement                                                                                                                   |
| org.json:json                                             | `20201115`                    | [OrgJsonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/OrgJsonNode.Factory.html)         | <ul><li>org.json.JSONObject,</li><li>org.json.JSONArray,</li><li>[literal types](#provider-literal-types).</li></ul>                                     |
| new.minidev:json-smart                                    | `1.1`                         | [JsonSmartNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JsonSmartNode.Factory.html)     | <ul><li>net.minidev.json.JSONObject,</li><li>net.minidev.json.JSONArray,</li><li>[literal types](#provider-literal-types).</li></ul>                     |
| org.codehouse.jettison:jettison                           | `1.5.0`                       | [JettisonNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/JettisonNode.Factory.html)       | <ul><li>org.codehaus.jettison.json.JSONObject,</li><li>org.codehaus.jettison.json.JSONArray,</li><li>[literal types](#provider-literal-types).</li></ul> |
| org.yaml:snakeyaml                                        | `1.26`                        | [SnakeYamlNode.Factory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/SnakeYamlNode.Factory.html)     | org.yaml.snakeyaml.nodes.Node                                                                                                                            |

Both newest and oldest versions are tested for each provider. 
The newest supported versions can be found in project POM (uploaded to maven central) listed as optional dependencies.

All adapter classes for JSON provider libs can be found in this [package](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/providers/package-summary.html). Anyone is free to add new adapter classes for any JSON lib of their choice, but keep in mind that it is not trivial. If you do so, ensure that test suites for providers pass.

### Changing JSON/YAML provider

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
It would be required to also have e.g. `org.glassfish:jakarta.json` or `org.eclipse.parsson:parsson`.
```java
new ValidatorFactory().withJsonNodeFactory(new JakartaJsonNode.Factory());
```

#### org.jetbrains.kotlinx:kotlinx-serialization-json
```java
new ValidatorFactory().withJsonNodeFactory(new KotlinxJsonNode.Factory());
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

#### org.yaml:snakeyaml
Library is compatible with YAML 1.1 standard. However, there are few constraints:
- all object keys are treated as strings,
- object keys cannot be duplicated,
- anchors and aliases are supported while override syntax (`<<`) is not.
```java
new ValidatorFactory().withJsonNodeFactory(new SnakeYamlNode.Factory());
```

If you expect schemas to be in JSON format and data in YAML format, you can use the following method:
```java
new ValidatorFactory().withJsonNodeFactories(otherJsonFactory, new SnakeYamlNode.Factory());
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

## Format validation
By default, `format` keyword performs no validation (only collects annotations as mandated by the JSON Schema specification).
If you want to use format validation, please add an explicit dependency to `jmail` library ([maven link](https://mvnrepository.com/artifact/com.sanctionco.jmail/jmail)):
```xml
<dependency>
  <groupId>com.sanctionco.jmail</groupId>
  <artifactId>jmail</artifactId>
  <version>1.6.3</version>
</dependency>
```
```groovy
implementation 'com.sanctionco.jmail:jmail:1.6.3'
```

To enable format validation, attach `FormatEvaluatorFactory` to your `ValidatorFactory` instance:
```java
new ValidatorFactory().withEvaluatorFactory(new FormatEvaluatorFactory());
```
If usage of another custom `EvaluatorFactory` is required, you can use `EvaluatorFactory.compose()` method:
```java
new ValidatorFactory().withEvaluatorFactory(EvaluatorFactory.compose(customFactory, new FormatEvaluatorFactory()));
```

#### Supported formats
- **date**, **date-time**, **time** - uses `java.time.format.DateTimeFormatter` with standard ISO formatters,
- **duration** - uses regex validation as it may be combination of `java.time.Duration` and `java.time.Period`,
- **email**, **idn-email** - uses `com.sanctionco.jmail.JMail`,
- **hostname** - uses regex validation,
- **idn-hostname** - not supported - performs same validation as `hostname`,
- **ipv4**, **ipv6** - uses `com.sanctionco.jmail.net.InternetProtocolAddress`,
- **uri**, **uri-reference**, **iri**, **iri-reference** - uses `java.net.URI`,
- **uuid** - uses `java.util.UUID`,
- **uri-template** - lenient checking of unclosed braces (should be compatible with Spring's implementation),
- **json-pointer**, **relative-json-pointer** - uses manual validation,
- **regex** - uses `java.util.regex.Pattern`.

Note that provided format validation is **not** 100% specification compliant.
Instead, it focuses to be more "Java environment oriented".
So for example, when a `value` is validated as being in `uri-reference` format, it is guaranteed that `URI.create(value)` call will succeed.

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
Officially supported dialects:
- Draft 2020-12,
- Draft 2019-09,
- Draft 07,
- Draft 06,
- Draft 04

It is automatically inferred (by content of `$schema` keyword) which dialect to use.
If a schema does not contain `$schema` keyword, the default dialect will be used, which is [draft 2020-12 dialect](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Dialects.Draft2020Dialect.html).
It's possible to change the default by calling:
```java
new ValidatorFactory().withDefaultDialect(new Dialects.Draft2019Dialect()); // or any other dialect
```
Custom dialects are also supported, see more [here](#custom-dialects).

### Meta-schemas
Each schema is recommended to contain `$schema` keyword to properly infer which dialect to use.
`$schema` keyword must refer to a meta-schema against which the current schema will be validated.
Resolution of meta-schemas follows the same [rules](#resolving-external-schemas) as for a regular schemas.
If validation against meta-schema fails [InvalidSchemaException](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/InvalidSchemaException.html) is thrown.

There is a configuration option that disables all schema validations:
```java
new ValidatorFactory().withDisabledSchemaValidation(true);
```

### Adding custom keywords
Customizing specific keywords behaviour can be achieved by providing custom [EvaluatorFactory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/EvaluatorFactory.html) implementation.
Each dialect comes with its core *EvaluatorFactory* which will always be used, but additional *EvaluatorFactory* implementation can be provided on top of that.
If you want to completely alter how schemas are validated, please refer to [custom dialects](#custom-dialects).

First step is to implement `Evaluator` interface:
```java
class ContainsStringEvaluator implements Evaluator {
    /* A value which should be contained in a validated string */
    private final String value;

    ContainsStringEvaluator(JsonNode node) {
        /* Other types are not supported - this exception will be handled appropriately by factory returned by the builder */
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.value = node.asString();
    }
    
    @Override
    public Evaluator.Result evaluate(EvaluationContext ctx, JsonNode node) {
        /* To stay consistent with other keywords, types not applicable to this keyword should succeed */
        if (!node.isString()) {
            return Evaluator.Result.success();
        }
        
        /* Actual validation logic */
        if (node.asString().contains(value)) {
            return Evaluator.Result.success();
        } else {
            return Evaluator.Result.failure(String.format("\"%s\" does not contain required value [%s]", node.asString(), value));
        }
    }
}
```

For the simplest cases (like this one) it is recommended to use [EvaluatorFactory.Builder](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/EvaluatorFactory.Builder.html).
This example shows how to create an evaluator factory using builder:
```java
EvaluatorFactory factory = new EvaluatorFactory.Builder()
    .withKeyword("containsString", ContainsStringEvaluator::new)
    .build();
```

For more complex cases when you need more control over creation of evaluators, you should provide your own factory implementation:
```java
class CustomEvaluatorFactory implements EvaluatorFactory {
    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode schemaNode) {
        /* Check if fieldName equals the keyword value you want to support.
         * Additionally, check if the node type of this keyword field is a string - this is the only type which makes sense in this case.
         * It may be tempting to fail if the keyword field type is different than string,
         * but it's strongly recommended to just return Optional.empty() in such case. */
        if ("containsString".equals(fieldName) && schemaNode.isString()) {
            return Optional.of(new ContainsStringEvaluator(schemaNode));
        }
        return Optional.empty();
    }
}
```

Then the factory just needs to be attached to `ValidatorFactory`:
```java
new ValidatorFactory().withEvaluatorFactory(factory);
```

And if you have more than one custom evaluator factory, you should use [compose](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/EvaluatorFactory.html#compose(dev.harrel.jsonschema.EvaluatorFactory...)) function:
```java
new ValidatorFactory().withEvaluatorFactory(EvaluatorFactory.compose(new CustomEvaluatorFactory1(), new CustomEvaluatorFactory2()));
```

Having such configuration as above you would have following list of evaluator factories:
1. `CustomEvaluatorFactory1`,
2. `CustomEvaluatorFactory2`,
3. `Draft2020EvaluatorFactory` (the core evaluator factory provided by `Dialect` object, might be different depending on which dialect is set).

The ordering of factories is important as it will only query the next factory only if the previous one returned `Optional.empty()`.
This allows for overriding keywords logic easily.

*E.g. if `CustomEvaluatorFactory1` returns an evaluator for `type` keyword, the `type` evaluator from `Draft2020EvaluatorFactory` would not be provided.*

Each *keyword* & *keyword-value* pair can have 0 or 1 evaluators attached.
If you want multiple evaluators attached to a single *keyword* & *keyword-value* pair, 
you would need to simulate such behavior by encapsulating multiple evaluators logic in just one evaluator instance.

#### Difference between schema parsing and validation

The implementation logic consist of two parts:
1. **Schema parsing** - running `EvaluatorFactory.create(...)` logic and constructing concrete `Evaluator` instance. You probably want to do as much computation heavy processing in this part.
2. **Validation** - running `Evaluator.evaluate(...)`.

Please ensure that your `EvaluatorFactory` and `Evaluator` implementations correctly handle cases when node types are different than expected.
This is because `EvaluatorFactory.create(...)` will be called for all schema object properties (nested too). So for example this may be intended usage:
```js
{
  "containsString": "hello"
}
```
but the `EvaluatorFactory` will be called also in this case:
```js
{
  "properties": {
    "containsString": {
      "type": "null"
    }
  }
}
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
This way, whenever `$schema` has value `https://example.com/custom/schema` this custom dialect will be used.
Please note that it is still required to provide a meta-schema which could be resolved from the given URI.
You can provide multiple custom dialects by multiple `withDialect()` calls.
See the [documentation](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Dialect.html) for more details.
