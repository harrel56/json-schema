package org.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class SchemaValidatorTest {

    @Test
    void name() throws IOException {
        SchemaValidator schemaValidator = new SchemaValidator();
        String rawSchema = new String(getClass().getResourceAsStream("/schema.json").readAllBytes());
        String rawJson = new String(getClass().getResourceAsStream("/json.json").readAllBytes());
        var res = schemaValidator.validate(rawSchema, rawJson);
        System.out.println(res);
    }
}