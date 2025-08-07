# Format assertions

There is official vocabulary for format assertions, although it is not supported by any official dialect.
To support it, you need to create and register a new dialect.
Note that URI `https://json-schema.org/draft/2020-12/schema-with-format-validation` was chosen arbitrarily,
and could possibly have any valid URI value.

## Dialect implementation
```java
import dev.harrel.jsonschema.*;

import java.util.HashSet;
import java.util.Set;

class FormatDialect extends Dialects.Draft2020Dialect {
    @Override
    public String getMetaSchema() {
        return "https://json-schema.org/draft/2020-12/schema-with-format-validation";
    }

    @Override
    public EvaluatorFactory getEvaluatorFactory() {
        return EvaluatorFactory.compose(new FormatEvaluatorFactory(Set.of(Vocabulary.Draft2020.FORMAT_ASSERTION)),
                new Draft2020EvaluatorFactory());
    }

    @Override
    public Set<String> getSupportedVocabularies() {
        Set<String> supported = new HashSet<>(super.getSupportedVocabularies());
        supported.add(Vocabulary.Draft2020.FORMAT_ASSERTION);
        return supported;
    }
}
```

## Meta-schema definition
```json
{
  "$id": "https://json-schema.org/draft/2020-12/schema-with-format-validation",
  "$ref": "https://json-schema.org/draft/2020-12/schema",
  "$vocabulary": {
      "https://json-schema.org/draft/2020-12/vocab/core": true,
      "https://json-schema.org/draft/2020-12/vocab/applicator": true,
      "https://json-schema.org/draft/2020-12/vocab/unevaluated": true,
      "https://json-schema.org/draft/2020-12/vocab/validation": true,
      "https://json-schema.org/draft/2020-12/vocab/meta-data": true,
      "https://json-schema.org/draft/2020-12/vocab/format-assertion": true,
      "https://json-schema.org/draft/2020-12/vocab/content": true
  }
}
```

## Example of schema definition
```json
{
  "$id": "urn:schema",
  "$schema": "https://json-schema.org/draft/2020-12/schema-with-format-validation",
  "format": "ipv4"
}
```

## Validator configuration
```java
Validator validator = new ValidatorFactory()
        .withDialect(new FormatDialect())
        .createValidator();
validator.registerSchema(metaSchemaWithFormatValidation);
validator.registerSchema(schema);

// It will fail validation as `test` is not a valid `ipv4` string
Validator.Result res = validator.validate(URI.create("urn:schema"), "\"test\"");
```