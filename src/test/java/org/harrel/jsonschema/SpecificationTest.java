package org.harrel.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;

import java.util.logging.Logger;

@SuppressWarnings("unused")
class SpecificationTest {

    private final Logger logger = Logger.getLogger("SpecificationTest");

    @SuiteTest("/draft2020-12/boolean_schema.json")
    void booleanSchemaTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/default.json")
    void defaultTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/format.json")
    void formatTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/content.json")
    void contentTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

//    @SuiteTest("/draft2020-12/unknownKeyword.json")
//    void unknownKeywordTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
//        testValidation(bundle, name, schema, json, valid);
//    }

//    @SuiteTest("/draft2020-12/id.json")
//    void idTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
//        testValidation(bundle, name, schema, json, valid);
//    }

    @SuiteTest("/draft2020-12/if-then-else.json")
    void ifThenElseTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/anyOf.json")
    void anyOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/allOf.json")
    void allOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/oneOf.json")
    void oneOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/not.json")
    void notTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

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

    @SuiteTest("/draft2020-12/items.json")
    void itemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/prefixItems.json")
    void prefixItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/maxItems.json")
    void maxItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/minItems.json")
    void minItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/uniqueItems.json")
    void uniqueItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/contains.json")
    void containsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/maxContains.json")
    void maxContainsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/minContains.json")
    void minContainsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/unevaluatedItems.json")
    void unevaluatedItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/maxProperties.json")
    void maxPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/additionalProperties.json")
    void additionalPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/patternProperties.json")
    void patternPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/unevaluatedProperties.json")
    void unevaluatedPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/minProperties.json")
    void minPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/required.json")
    void requiredTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/dependentRequired.json")
    void dependentRequiredTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/dependentSchemas.json")
    void dependentSchemasTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/ref.json")
    void refTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/anchor.json")
    void anchorTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/dynamicRef.json")
    void dynamicRefTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/pattern.json")
    void patternTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/maxLength.json")
    void maxLengthTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/minLength.json")
    void minLengthTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/maximum.json")
    void maximumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/minimum.json")
    void minimumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/draft2020-12/multipleOf.json")
    void multipleOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    private void testValidation(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
//        Assumptions.assumeTrue(bundle.equals("A $dynamicRef that initially resolves to a schema with a matching $dynamicAnchor resolves to the first $dynamicAnchor in the dynamic scope"));
//        Assumptions.assumeTrue(name.equals("The recursive part is not valid against the root"));
//        Assumptions.assumeTrue(bundle.equals("A $dynamicRef that initially resolves to a schema with a matching $dynamicAnchor resolves to the first $dynamicAnchor in the dynamic scope"));
//        Assumptions.assumeTrue(name.equals("The recursive part is not valid against the root"));
        SchemaValidator validator = new SchemaValidator();
        logger.info("%s: %s".formatted(bundle, name));
        logger.info(schema.toPrettyString());
        logger.info(json.toPrettyString());
        logger.info(String.valueOf(valid));
        Assertions.assertEquals(valid, validator.validate(new JacksonNode(schema), new JacksonNode(json)));
    }
}
