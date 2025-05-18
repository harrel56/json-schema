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