package dev.harrel.jsonschema;

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
                .withSchemaResolver(new RemoteSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, skippedRequiredTests());
        return generator.generate("/suite/tests/draft2020-12");
    }

    @TestFactory
    Stream<DynamicNode> draft2019Required() {
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2019Dialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(new RemoteSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, skippedRequiredTests());
        return generator.generate("/suite/tests/draft2019-09");
    }

    @TestFactory
    Stream<DynamicNode> draft2020Format() {
        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(new RemoteSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, skippedFormatTests());
        return generator.generate("/suite/tests/draft2020-12/optional/format");
    }

    @TestFactory
    Stream<DynamicNode> draft2019Format() {
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2019Dialect())
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(new RemoteSchemaResolver())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, skippedFormatTests());
        return generator.generate("/suite/tests/draft2019-09/optional/format");
    }

    @TestFactory
    Stream<DynamicNode> draft2020Optional() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, Map.of());
        return Stream.of(
                generator.generate("/suite/tests/draft2020-12/optional/bignum.json"),
                generator.generate("/suite/tests/draft2020-12/optional/no-schema.json"),
                generator.generate("/suite/tests/draft2020-12/optional/non-bmp-regex.json"),
                generator.generate("/suite/tests/draft2020-12/optional/refOfUnknownKeyword.json")
        ).flatMap(Function.identity());
    }

    @TestFactory
    Stream<DynamicNode> draft2019Optional() {
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2019Dialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, Map.of());
        return Stream.of(
                generator.generate("/suite/tests/draft2019-09/optional/bignum.json"),
                generator.generate("/suite/tests/draft2019-09/optional/no-schema.json"),
                generator.generate("/suite/tests/draft2019-09/optional/non-bmp-regex.json"),
                generator.generate("/suite/tests/draft2019-09/optional/refOfUnknownKeyword.json")
        ).flatMap(Function.identity());
    }

    private static Map<String, Map<String, Set<String>>> skippedRequiredTests() {
        return Map.of(
                "id.json", Map.of(
                        "$id inside an enum is not a real identifier", Set.of(
                                "match $ref to $id"
                        )
                ),
                "unknownKeyword.json", Map.of(
                        "$id inside an unknown keyword is not a real identifier", Set.of(
                                "type matches second anyOf, which has a real schema in it",
                                "type matches non-schema in first anyOf"
                        )
                )
        );
    }

    private static Map<String, Map<String, Set<String>>> skippedFormatTests() {
        return Map.of(
                "date-time.json", Map.of(
                        "validation of date-time strings", Set.of(
                                /* leap seconds not supported */
                                "a valid date-time with a leap second, UTC",
                                "a valid date-time with a leap second, with minus offset"
                        )
                ),
                "time.json", Map.of(
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
                "iri.json", Map.of(
                        "validation of IRIs", Set.of(
                                "an invalid IRI based on IPv6"
                        )
                ),
                "idn-hostname.json", Map.of(
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
                "ipv4.json", Map.of(
                        "validation of IP addresses", Set.of(
                                "invalid leading zeroes, as they are treated as octals",
                                "invalid non-ASCII '\u09e8' (a Bengali 2)"
                        )
                ),
                "ipv6.json", Map.of(
                        "validation of IPv6 addresses", Set.of(
                                "invalid non-ASCII '\u09ea' (a Bengali 4)",
                                "invalid non-ASCII '\u09ea' (a Bengali 4) in the IPv4 portion"
                        )
                )
        );
    }
}
