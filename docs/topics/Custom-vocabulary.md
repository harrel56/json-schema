# Custom vocabulary

This shows how to add support for custom keywords but only when your custom vocabulary is turned on.

## EvaluatorFactory implementation

Implementation of `CustomEvaluator` is omitted for brevity.
```java
import dev.harrel.jsonschema.Evaluator;
import dev.harrel.jsonschema.EvaluatorFactory;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SchemaParsingContext;

import java.util.Optional;

class CustomEvaluatorFactory implements EvaluatorFactory {
    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode) {
        if ("customKeyword".equals(fieldName) && ctx.getActiveVocabularies().contains("urn:custom-vocabulary")) {
            return Optional.of(new CustomEvaluator(ctx, fieldNode));
        }
        return Optional.empty();
    }
}
```

## Dialect implementation
```java
import dev.harrel.jsonschema.Dialects;
import dev.harrel.jsonschema.Draft2020EvaluatorFactory;
import dev.harrel.jsonschema.EvaluatorFactory;

import java.util.HashSet;
import java.util.Set;

class CustomDialect extends Dialects.Draft2020Dialect {
    @Override
    public String getMetaSchema() {
        return "urn:custom-dialect";
    }

    @Override
    public EvaluatorFactory getEvaluatorFactory() {
        return EvaluatorFactory.compose(new CustomEvaluatorFactory(), new Draft2020EvaluatorFactory());
    }

    @Override
    public Set<String> getSupportedVocabularies() {
        Set<String> supported = new HashSet<>(super.getSupportedVocabularies());
        supported.add("urn:custom-vocabulary");
        return supported;
    }
}
```

## Meta-schema definition
```json
{
  "$id": "urn:custom-dialect",
  "$ref": "https://json-schema.org/draft/2020-12/schema",
  "$vocabulary": {
    "https://json-schema.org/draft/2020-12/vocab/core": true,
    "https://json-schema.org/draft/2020-12/vocab/applicator": true,
    "https://json-schema.org/draft/2020-12/vocab/unevaluated": true,
    "https://json-schema.org/draft/2020-12/vocab/validation": true,
    "https://json-schema.org/draft/2020-12/vocab/meta-data": true,
    "https://json-schema.org/draft/2020-12/vocab/format-annotation": true,
    "https://json-schema.org/draft/2020-12/vocab/content": true,
    "urn:custom-vocabulary": true
  }
}
```

## Example of schema definition
```json
{
  "$id": "urn:schema",
  "$schema": "urn:custom-dialect",
  "customKeyword": "something"
}
```

## Validator configuration
```java
Validator validator = new ValidatorFactory()
    .withDialect(new CustomDialect())
    .createValidator();
validator.registerSchema(customMetaSchema);
validator.registerSchema(schema);
```