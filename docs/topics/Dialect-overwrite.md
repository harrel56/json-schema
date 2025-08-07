# Dialect overwrite

This shows how to effectively overwrite `draft4` dialect and treat it as `draft2020-12` dialect.

## Example of schema definition

Notice that `$id` was not a valid keyword in `draft4`, so it wouldn't be accessible under `urn:schema` URI,
if not for the dialect overwrite.
```json
{
  "$id": "urn:schema",
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "null"
}
```

## Validator configuration
```java
Validator validator = new ValidatorFactory()
        .withDialect(new Dialects.Draft2020Dialect() {
            @Override
            public String getMetaSchema() {
                return SpecificationVersion.DRAFT4.getId();
            }
        })
        .createValidator();
validator.registerSchema(schema);
Validator.Result res = validator.validate(URI.create("urn:schema"), "\"test\"");
```