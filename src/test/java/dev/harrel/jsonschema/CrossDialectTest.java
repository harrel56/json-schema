package dev.harrel.jsonschema;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CrossDialectTest {
    @Test
    @Disabled
    void compoundSchemaDoesntValidateEmbeddedSchemas() {
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$ref": "#/$defs/draft2019",
                  "$defs": {
                    "draft2019": {
                      "$schema": "https://json-schema.org/draft/2019-09/schema",
                      "$id": "urn:nested",
                      "items": [{
                        "type": "string"
                      }]
                    }
                  }
                }""";

        Validator.Result result = new ValidatorFactory()
                .validate(schema, "[1]");
        System.out.println(result);
    }
}
