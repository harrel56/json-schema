# Basic usage

To validate JSON against a schema, you need to invoke:
```java
String schema = """
        {
          "type": "boolean"
        }""";
String instance = "true";
boolean valid = new ValidatorFactory()
        .validate(schema, instance)
        .isValid();
```

Validation result could be queried for more verbose output than a simple boolean flag:
```java
Validator.Result result = new ValidatorFactory().validate(schema, instance);

// Boolean flag indicating if validation succeeded
boolean valid = result.isValid();

// Details where validation exactly failed
List<Error> errors = result.getErrors();

// Collected annotation during the validation process
List<Annotation> annotations = result.getAnnotations(); 
```

## Reusing schema
Probably the most common case is to validate multiple JSON objects against one specific schema.
The approach listed above parses schema for each validation request.
To avoid this performance hit, it is better to use [Validator](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Validator.html) class directly.
```java
Validator validator = new ValidatorFactory().createValidator();

// Returns URI which should be used to refer to this schema
URI schemaUri = validator.registerSchema(schema);
Validator.Result result1 = validator.validate(schemaUri, instance1);
Validator.Result result2 = validator.validate(schemaUri, instance2);
```
This way, the schema is parsed only once.
You could also register multiple schemas this way and refer to them independently. 
Keep in mind that the "registration space" for schemas is common for one `Validator` - this can be used to refer dynamically between schemas.

> You can also register a schema under a specific URI: `validator.registerSchema(uri, schema)`.

> If the `$id` keyword is present, it will also be registered under it.

### Error type

<deflist>
<def title='error (String)'>
Validation message.

Example: `Value is [null] but should be [number]`
</def>
<def title='evaluationPath (String)'>
JSON pointer representing evaluation point in schema JSON.

Example: `/properties/foo/type`
</def>
<def title='instanceLocation (String)'>
JSON pointer representing evaluation point in instance JSON.

Example: `/foo`
</def>
<def title='keyword (String)'>
Keyword name associated with given evaluation point.

Example: `type`
</def>
<def title='schemaLocation (String)'>
Absolute schema location (URI) which uniquely identifies given schema.

Example: `https://harrel.dev/3077cb97#/properties/foo`
</def>
</deflist>

### Annotation type
Please see a dedicated chapter: [Annotations](Annotations.md).

## Thread safety
- `ValidatorFactory` **IS NOT** thread-safe as it contains mutable configuration elements which may lead to memory visibility issues.
  `validate(...)` methods are, however, stateless, so if the factory is configured before it has been shared between threads, it can be used concurrently.
- `Validator` **IS** thread-safe as its configuration is immutable. The internal schema registry is configured for a multi-threaded usage.
- All the library provided implementations (`SchemaResolver`, `JsonNodeFactory`, `EvaluatorFactory`, `Evaluator`) are thread safe.
  For custom user implementations: if intended for use in a multi-threaded environment, the implementation should ensure thread safety.