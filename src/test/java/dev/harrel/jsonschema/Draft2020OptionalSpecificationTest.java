package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.util.RemoteSchemaResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

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

    @TestFactory
    Stream<DynamicNode> generateTest() throws URISyntaxException, IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        URL url = getClass().getResource("/suite/tests/draft2020-12/optional/format");
        Path path = Paths.get(url.toURI());
        List<Path> list = Files.list(path).toList();
        return Files.list(path).map(p -> DynamicContainer.dynamicContainer(p.getFileName().toString(), readTestFile(p)));
    }

    private Stream<DynamicContainer> readTestFile(Path path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String rawString = Files.readString(path);
            List<SchemaTest> bundles = objectMapper.readValue(rawString, new TypeReference<>() {});
            return bundles.stream()
                    .map(bundle -> DynamicContainer.dynamicContainer(bundle.description, bundle.tests.stream()
                            .map(test -> DynamicTest.dynamicTest(test.description, path.toUri(),
                                    () -> testValidation(bundle.description, test.description, bundle.schema, test.data, test.valid)))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
//        return bundles.stream()
//                .flatMap(bundle -> bundle.tests().stream().map(test ->
//                        Arguments.arguments(bundle.description(),
//                                test.description(),
//                                bundle.schema(),
//                                test.data(),
//                                test.valid())));
    }


    record SchemaTest(String description, JsonNode schema, List<TestData> tests) {}

    record TestData(String description, JsonNode data, boolean valid) {}

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
