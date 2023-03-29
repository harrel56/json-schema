package org.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

class SchemaValidatorTest {

    @Test
    void name() throws IOException {
        SchemaValidator schemaValidator = SchemaValidator.builder().build();
        String rawSchema = new String(getClass().getResourceAsStream("/schema.json").readAllBytes());
        String rawJson = new String(getClass().getResourceAsStream("/json.json").readAllBytes());
        URI uri = URI.create(getClass().getSimpleName());
        schemaValidator.registerSchema(uri, rawSchema);
        var res = schemaValidator.validate(uri, rawJson);
        System.out.println(res);
    }
}