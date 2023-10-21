package dev.harrel.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static dev.harrel.jsonschema.TestUtil.readResource;

@SuppressWarnings("unused")
public abstract class SpecificationTest {
    static final Logger logger = Logger.getLogger("SpecificationTest");
    protected static JsonNodeFactory nodeFactory;

    @SuiteTest("/suite/draft2020-12/boolean_schema.json")
    void booleanSchemaTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/default.json")
    void defaultTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/format.json")
    void formatTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/content.json")
    void contentTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/defs.json")
    void defsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @Disabled("$id in all places are supported")
    @SuiteTest("/suite/draft2020-12/unknownKeyword.json")
    void unknownKeywordTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/vocabulary.json")
    void vocabularyTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/id.json")
    void idTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/infinite-loop-detection.json")
    void infiniteLoopDetectionTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/if-then-else.json")
    void ifThenElseTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/anyOf.json")
    void anyOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/allOf.json")
    void allOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/oneOf.json")
    void oneOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/not.json")
    void notTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/type.json")
    void typeTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/const.json")
    void constTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/enum.json")
    void enumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/items.json")
    void itemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/prefixItems.json")
    void prefixItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/maxItems.json")
    void maxItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/minItems.json")
    void minItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/uniqueItems.json")
    void uniqueItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/contains.json")
    void containsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/maxContains.json")
    void maxContainsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/minContains.json")
    void minContainsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/unevaluatedItems.json")
    void unevaluatedItemsTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/properties.json")
    void propertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/maxProperties.json")
    void maxPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/additionalProperties.json")
    void additionalPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/patternProperties.json")
    void patternPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/propertyNames.json")
    void propertyNamesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/unevaluatedProperties.json")
    void unevaluatedPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/minProperties.json")
    void minPropertiesTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/required.json")
    void requiredTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/dependentRequired.json")
    void dependentRequiredTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/dependentSchemas.json")
    void dependentSchemasTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/ref.json")
    void refTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/refRemote.json")
    void refRemoteTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/anchor.json")
    void anchorTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/dynamicRef.json")
    void dynamicRefTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/pattern.json")
    void patternTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/maxLength.json")
    void maxLengthTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/minLength.json")
    void minLengthTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/maximum.json")
    void maximumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/minimum.json")
    void minimumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/exclusiveMaximum.json")
    void exclusiveMaximumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/exclusiveMinimum.json")
    void exclusiveMinimumTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/draft2020-12/multipleOf.json")
    void multipleOfTest(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    private static SchemaResolver resolver;

    @BeforeAll
    static void resolverSetup() {
        Map<String, String> schemaMap = Map.ofEntries(
                Map.entry("https://json-schema.org/draft/2020-12/schema", readResource("/schemas/draft2020-12.json")),
                Map.entry("http://localhost:1234/different-id-ref-string.json", readResource("/schemas/different-id-ref-string.json")),
                Map.entry("http://localhost:1234/nested-absolute-ref-to-string.json", readResource("/schemas/nested-absolute-ref-to-string.json")),
                Map.entry("http://localhost:1234/urn-ref-string.json", readResource("/schemas/urn-ref-string.json")),
                Map.entry("http://localhost:1234/draft2020-12/detached-dynamicref.json", readResource("/schemas/detached-dynamicref.json")),
                Map.entry("http://localhost:1234/draft2020-12/detached-ref.json", readResource("/schemas/detached-ref.json")),
                Map.entry("http://localhost:1234/draft2020-12/extendible-dynamic-ref.json", readResource("/schemas/extendible-dynamic-ref.json")),
                Map.entry("http://localhost:1234/draft2020-12/integer.json", readResource("/schemas/integer.json")),
                Map.entry("http://localhost:1234/draft2020-12/locationIndependentIdentifier.json", readResource("/schemas/locationIndependentIdentifier.json")),
                Map.entry("http://localhost:1234/draft2020-12/metaschema-no-validation.json", readResource("/schemas/metaschema-no-validation.json")),
                Map.entry("http://localhost:1234/draft2020-12/metaschema-optional-vocabulary.json", readResource("/schemas/metaschema-optional-vocabulary.json")),
                Map.entry("http://localhost:1234/draft2020-12/name-defs.json", readResource("/schemas/name-defs.json")),
                Map.entry("http://localhost:1234/draft2020-12/ref-and-defs.json", readResource("/schemas/ref-and-defs.json")),
                Map.entry("http://localhost:1234/draft2020-12/subSchemas-defs.json", readResource("/schemas/subSchemas-defs.json")),
                Map.entry("http://localhost:1234/draft2020-12/tree.json", readResource("/schemas/tree.json")),
                Map.entry("http://localhost:1234/draft2020-12/baseUriChange/folderInteger.json", readResource("/schemas/baseUriChange/folderInteger.json")),
                Map.entry("http://localhost:1234/draft2020-12/baseUriChangeFolder/folderInteger.json", readResource("/schemas/baseUriChangeFolder/folderInteger.json")),
                Map.entry("http://localhost:1234/draft2020-12/baseUriChangeFolderInSubschema/folderInteger.json", readResource("/schemas/baseUriChangeFolderInSubschema/folderInteger.json")),
                Map.entry("http://localhost:1234/draft2020-12/nested/foo-ref-string.json", readResource("/schemas/nested/foo-ref-string.json")),
                Map.entry("http://localhost:1234/draft2020-12/nested/string.json", readResource("/schemas/nested/string.json"))
        );
        resolver = uri -> Optional.ofNullable(schemaMap.get(uri))
                .map(SchemaResolver.Result::fromString)
                .orElse(SchemaResolver.Result.empty());
    }

    private void testValidation(String bundle, String name, JsonNode schema, JsonNode instance, boolean valid) {
//        Assumptions.assumeTrue(bundle.equals("tests for implementation dynamic anchor and reference link"));
//        Assumptions.assumeTrue(name.equals("correct extended schema"));
        String schemaString = schema.toPrettyString();
        String instanceString = instance.toPrettyString();
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .createValidator();
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
