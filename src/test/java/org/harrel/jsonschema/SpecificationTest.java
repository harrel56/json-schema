package org.harrel.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;

import java.util.logging.Logger;

class SpecificationTest {

    private final Logger logger = Logger.getLogger("SpecificationTest");

    @SuiteTest("/draft2020-12/type.json")
    void typeTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/const.json")
    void constTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/enum.json")
    void enumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/required.json")
    void requiredTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    private void testValidation(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        SchemaValidator validator = new SchemaValidator();
        logger.info(schema.toPrettyString());
        logger.info(json.toPrettyString());
        logger.info(String.valueOf(valid));
        Assertions.assertEquals(valid, validator.validate(new JacksonNode(schema), new JacksonNode(json)));
    }
}
