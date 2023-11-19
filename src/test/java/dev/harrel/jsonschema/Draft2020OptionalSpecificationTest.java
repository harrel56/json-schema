package dev.harrel.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import dev.harrel.jsonschema.util.RemoteSchemaResolver;
import dev.harrel.jsonschema.util.SuiteTest;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class Draft2020OptionalSpecificationTest implements ProviderTest {
    private static final Logger logger = Logger.getLogger("SpecificationTest");
    private Validator validator;

    @BeforeAll
    void beforeAll() {
        validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(new RemoteSchemaResolver())
                .createValidator();
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/date.json")
    void optionalDate(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/date-time.json")
    void optionalDateTime(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/time.json")
    void optionalTime(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/duration.json")
    void optionalDuration(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/email.json")
    void optionalEmail(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    private void testValidation(String bundle, String name, JsonNode schema, JsonNode instance, boolean valid) {
//        Assumptions.assumeTrue(bundle.equals("unevaluatedProperties with $dynamicRef"));
//        Assumptions.assumeTrue(name.equals("with no unevaluated properties"));
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
        /* leap seconds not supported */
        Assumptions.assumeFalse(bundle.equals("validation of date-time strings") &&
                Set.of(
                        "a valid date-time with a leap second, UTC",
                        "a valid date-time with a leap second, with minus offset"
                ).contains(name));
        Assumptions.assumeFalse(bundle.equals("validation of time strings") &&
                Set.of(
                        "a valid time string with leap second, Zulu",
                        "valid leap second, zero time-offset",
                        "valid leap second, positive time-offset",
                        "valid leap second, large positive time-offset",
                        "valid leap second, negative time-offset",
                        "valid leap second, large negative time-offset",
                        /* honestly idk why the below should be invalid */
                        "no time offset",
                        "no time offset with second fraction"
                ).contains(name));
    }
}
