package dev.harrel.jsonschema;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.*;

class CrossDialectTest {
    @Test
    @Disabled
    // fixme
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
