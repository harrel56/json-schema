package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.harrel.jsonschema.TestUtil.readResource;

class ValidatorFactoryTest {

    @Test
    void name() {
        String schema = readResource("/schema.json");
        String instance = readResource("/instance.json");
        Validator.Result result = new ValidatorFactory().withDefaultMetaSchemaUri(null).validate(schema, instance);
        List<Error> errors = result.getErrors();
        System.out.println(errors);
    }
}