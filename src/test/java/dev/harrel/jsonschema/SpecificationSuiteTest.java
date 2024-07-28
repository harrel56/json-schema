package dev.harrel.jsonschema;

import dev.harrel.jsonschema.util.ProviderMapper;
import dev.harrel.jsonschema.util.RemoteSchemaResolver;
import dev.harrel.jsonschema.util.SuiteTestGenerator;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class SpecificationSuiteTest implements ProviderTest {

    @TestFactory
    Stream<DynamicNode> draft2020Required() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(createSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return generator.generate(getTestPath() + "/draft2020-12");
    }

    @TestFactory
    Stream<DynamicNode> draft2019Required() {
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft2019Dialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(createSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return generator.generate(getTestPath() + "/draft2019-09");
    }

    @TestFactory
    Stream<DynamicNode> draft7Required() {
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft7Dialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(createSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), validator, skippedDraft7Tests());
        return generator.generate(getTestPath() + "/draft7");
    }

    @TestFactory
    Stream<DynamicNode> draft2020Format() {
        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(createSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), validator, skippedFormatTests());
        return generator.generate(getTestPath() + "/draft2020-12/optional/format");
    }

    @TestFactory
    Stream<DynamicNode> draft2019Format() {
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft2019Dialect())
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(createSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), validator, skippedFormatTests());
        return generator.generate(getTestPath() + "/draft2019-09/optional/format");
    }

    @TestFactory
    Stream<DynamicNode> draft2020Optional() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(createSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return Stream.of(
                generator.generate(getTestPath() + "/draft2020-12/optional/bignum" + getFileExtension()),
                generator.generate(getTestPath() + "/draft2020-12/optional/cross-draft" + getFileExtension()),
                generator.generate(getTestPath() + "/draft2020-12/optional/no-schema" + getFileExtension()),
                generator.generate(getTestPath() + "/draft2020-12/optional/non-bmp-regex" + getFileExtension()),
                generator.generate(getTestPath() + "/draft2020-12/optional/refOfUnknownKeyword" + getFileExtension())
        ).flatMap(Function.identity());
    }

    @TestFactory
    Stream<DynamicNode> draft2019Optional() {
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft2019Dialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(createSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return Stream.of(
                generator.generate(getTestPath() + "/draft2019-09/optional/bignum" + getFileExtension()),
                generator.generate(getTestPath() + "/draft2019-09/optional/cross-draft" + getFileExtension()),
                generator.generate(getTestPath() + "/draft2019-09/optional/no-schema" + getFileExtension()),
                generator.generate(getTestPath() + "/draft2019-09/optional/non-bmp-regex" + getFileExtension()),
                generator.generate(getTestPath() + "/draft2019-09/optional/refOfUnknownKeyword" + getFileExtension())
        ).flatMap(Function.identity());
    }

    SchemaResolver createSchemaResolver() {
        return new RemoteSchemaResolver();
    }

    String getTestPath() {
        return "/suite/tests";
    }

    String getFileExtension() {
        return ".json";
    }

    private static Map<String, Map<String, Set<String>>> skippedDraft7Tests() {
        return Map.of(
                "ref", Map.of(
                        "$ref prevents a sibling $id from changing the base uri", Set.of(
                                "$ref resolves to /definitions/base_foo, data does not validate",
                                "$ref resolves to /definitions/base_foo, data validates"
                        )
                )
        );
    }


    private static Map<String, Map<String, Set<String>>> skippedFormatTests() {
        return Map.of(
                "date-time", Map.of(
                        "validation of date-time strings", Set.of(
                                /* leap seconds not supported */
                                "a valid date-time with a leap second, UTC",
                                "a valid date-time with a leap second, with minus offset"
                        )
                ),
                "time", Map.of(
                        "validation of time strings", Set.of(
                                /* leap seconds not supported */
                                "a valid time string with leap second, Zulu",
                                "valid leap second, zero time-offset",
                                "valid leap second, positive time-offset",
                                "valid leap second, large positive time-offset",
                                "valid leap second, negative time-offset",
                                "valid leap second, large negative time-offset",
                                /* honestly idk why the below should be invalid */
                                "no time offset",
                                "no time offset with second fraction"
                        )
                ),
                "iri", Map.of(
                        "validation of IRIs", Set.of(
                                "an invalid IRI based on IPv6"
                        )
                ),
                "idn-hostname", Map.of(
                        "validation of internationalized host names", Set.of(
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
                        )
                ),
                "ipv4", Map.of(
                        "validation of IP addresses", Set.of(
                                "invalid leading zeroes, as they are treated as octals"
                        )
                )
        );
    }
}
