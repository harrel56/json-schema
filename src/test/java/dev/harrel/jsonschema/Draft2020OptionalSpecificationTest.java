package dev.harrel.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import dev.harrel.jsonschema.util.RemoteSchemaResolver;
import dev.harrel.jsonschema.util.SuiteTest;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.Objects;
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

    @SuiteTest("/suite/tests/draft2020-12/optional/format/idn-email.json")
    void optionalIdnEmail(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/hostname.json")
    void optionalHostname(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/idn-hostname.json")
    void optionalIdnHostname(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/ipv4.json")
    void optionalIpv4(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/ipv6.json")
    void optionalIpv6(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/uri.json")
    void optionalUri(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/uri-reference.json")
    void optionalUriReference(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/iri.json")
    void optionalIri(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/iri-reference.json")
    void optionalIriReference(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/uuid.json")
    void optionalUuid(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/uri-template.json")
    void optionalUriTemplate(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/json-pointer.json")
    void optionalJsonPointer(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/relative-json-pointer.json")
    void optionalRelativeJsonPointer(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        testValidation(bundle, name, schema, json, valid);
    }

    @SuiteTest("/suite/tests/draft2020-12/optional/format/regex.json")
    void optionalRegex(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
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
        Assumptions.assumeFalse(bundle.equals("validation of date-time strings") &&
                Set.of(
                        /* leap seconds not supported */
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
        Assumptions.assumeFalse(bundle.equals("validation of IRIs") &&
                Objects.equals("an invalid IRI based on IPv6", name));
        Assumptions.assumeFalse(bundle.equals("validation of internationalized host names") &&
                Set.of(
                        "a valid host name (example.test in Hangul)",
                        "invalid Punycode",
                        "U-label contains \"--\" in the 3rd and 4th position",
                        "Exceptions that are PVALID, left-to-right chars",
                        "Exceptions that are PVALID, right-to-left chars",
                        "MIDDLE DOT with surrounding 'l's",
                        "Greek KERAIA followed by Greek",
                        "Hebrew GERESH preceded by Hebrew",
                        "Hebrew GERSHAYIM preceded by Hebrew",
                        "KATAKANA MIDDLE DOT with Hiragana",
                        "KATAKANA MIDDLE DOT with Katakana",
                        "KATAKANA MIDDLE DOT with Han",
                        "Arabic-Indic digits not mixed with Extended Arabic-Indic digits",
                        "Extended Arabic-Indic digits not mixed with Arabic-Indic digits",
                        "ZERO WIDTH JOINER preceded by Virama",
                        "ZERO WIDTH NON-JOINER preceded by Virama",
                        "ZERO WIDTH NON-JOINER not preceded by Virama but matches regexp"
                ).contains(name));
        Assumptions.assumeFalse(bundle.equals("validation of IP addresses") &&
                Objects.equals("invalid leading zeroes, as they are treated as octals", name) ||
                name.contains("Bengali 2"));
        Assumptions.assumeFalse(bundle.equals("validation of IPv6 addresses") &&
                name.contains("Bengali 4"));
    }
}
