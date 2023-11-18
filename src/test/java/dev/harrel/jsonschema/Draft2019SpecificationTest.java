package dev.harrel.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import dev.harrel.jsonschema.util.RemoteSchemaResolver;
import dev.harrel.jsonschema.util.SuiteTest;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class Draft2019SpecificationTest implements ProviderTest {
    private static final Logger logger = Logger.getLogger("SpecificationTest");
    private Validator validator;

    @BeforeAll
    void beforeAll() {
        validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2019Dialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(new RemoteSchemaResolver())
                .createValidator();
    }

    @SuiteTest("/suite/tests/draft2019-09/boolean_schema.json")
    void booleanSchemaTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/default.json")
    void defaultTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/format.json")
    void formatTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/content.json")
    void contentTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/defs.json")
    void defsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @Disabled("$id in all places are supported")
    @SuiteTest("/suite/tests/draft2019-09/unknownKeyword.json")
    void unknownKeywordTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/vocabulary.json")
    void vocabularyTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/id.json")
    void idTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/infinite-loop-detection.json")
    void infiniteLoopDetectionTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/if-then-else.json")
    void ifThenElseTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/anyOf.json")
    void anyOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/allOf.json")
    void allOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/oneOf.json")
    void oneOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/not.json")
    void notTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/type.json")
    void typeTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/const.json")
    void constTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/enum.json")
    void enumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/items.json")
    void itemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/additionalItems.json")
    void additionalItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/maxItems.json")
    void maxItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/minItems.json")
    void minItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/uniqueItems.json")
    void uniqueItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/contains.json")
    void containsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/maxContains.json")
    void maxContainsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/minContains.json")
    void minContainsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/unevaluatedItems.json")
    void unevaluatedItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/properties.json")
    void propertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/maxProperties.json")
    void maxPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/additionalProperties.json")
    void additionalPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/patternProperties.json")
    void patternPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/propertyNames.json")
    void propertyNamesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/unevaluatedProperties.json")
    void unevaluatedPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/minProperties.json")
    void minPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/required.json")
    void requiredTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/dependentRequired.json")
    void dependentRequiredTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/dependentSchemas.json")
    void dependentSchemasTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/ref.json")
    void refTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/refRemote.json")
    void refRemoteTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/anchor.json")
    void anchorTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/recursiveRef.json")
    void recursiveRefTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/pattern.json")
    void patternTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/maxLength.json")
    void maxLengthTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/minLength.json")
    void minLengthTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/maximum.json")
    void maximumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/minimum.json")
    void minimumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/exclusiveMaximum.json")
    void exclusiveMaximumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/exclusiveMinimum.json")
    void exclusiveMinimumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2019-09/multipleOf.json")
    void multipleOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    private void testValidation(String bundle, String name, JsonNode schema, JsonNode instance, boolean valid) {
//        Assumptions.assumeTrue(bundle.equals("unevaluatedProperties can't see inside cousins"));
//        Assumptions.assumeTrue(name.equals("always fails"));
        String schemaString = schema.toPrettyString();
        String instanceString = instance.toPrettyString();
        logger.info("%s: %s".formatted(bundle, name));
        logger.info(schemaString);
        logger.info(instanceString);
        logger.info(String.valueOf(valid));
        skipUnsupportedTests(bundle, name);
        URI uri = validator.registerSchema(schemaString);
        Assertions.assertEquals(valid, validator.validate(uri, instanceString).isValid());
    }

    private void skipUnsupportedTests(String bundle, String name) {
        Assumptions.assumeFalse(bundle.equals("$id inside an enum is not a real identifier"));
        Assumptions.assumeFalse(bundle.equals("$anchor inside an enum is not a real identifier"));
    }
}
