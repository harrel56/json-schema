package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static dev.harrel.jsonschema.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class UriEquivalenceTest {

    @ParameterizedTest
    @MethodSource("getEquivalentUris")
    void urisAreEquivalent(String ref, String id) {
        String schema = """
                {
                  "$ref": "%s",
                  "$defs": {
                    "x": {
                      "$id": "%s",
                      "type": "object"
                    }
                  }
                }
                """.formatted(ref, id);

        Validator.Result result = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .validate(schema, "{}");

        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getNonEquivalentUris")
    void urisDontMatch(String ref, String id) {
        String schema = """
                {
                  "$ref": "%s",
                  "$defs": {
                    "x": {
                      "$id": "%s",
                      "type": "object"
                    }
                  }
                }
                """.formatted(ref, id);

        Validator.Result result = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .validate(schema, "{}");

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/$ref",
                "https://harrel.dev",
                "",
                "$ref"
        );
    }

    private static Stream<Arguments> getEquivalentUris() {
        return Stream.of(
                Arguments.of("HTTPS://test.com", "https://test.com"),
                Arguments.of("https://test.com", "HTTPS://test.com"),
                Arguments.of("https://test.com?a=1", "https://test.com?a=1"),
                Arguments.of("https://test.com?a=1&b=2", "https://test.com?a=1&b=2"),
                Arguments.of("fiLE:///test.json", "file:///test.json"),
                Arguments.of("file:///test.json", "FIle:///test.json"),
                Arguments.of("URN:test", "urn:test"),
                Arguments.of("urn:test", "URN:test"),
                Arguments.of("https://test.com", "HTTPS://test.com"),
                Arguments.of("https://test.COM", "https://test.com"),
                Arguments.of("https://test.com", "https://test.COM"),
                Arguments.of("https://user@test.com", "https://user@test.com"),
                Arguments.of("tcp://test.com:8080", "tcp://test.com:8080"),
                Arguments.of("https://TEST.com", "https://test.com"),
                Arguments.of("https://test.com", "https://TEST.com"),
                Arguments.of("https://test.com/%2f", "https://test.com/%2F"),
                Arguments.of("https://test.com/%2F", "https://test.com/%2f"),
                Arguments.of("https://test.com/x%2dx", "https://test.com/x%2Dx"),
                Arguments.of("https://test.com/x%2Dx", "https://test.com/x%2dx")
        );
    }

    private static Stream<Arguments> getNonEquivalentUris() {
        return Stream.of(
                Arguments.of("https://test.com/", "https://test.com"),
                Arguments.of("https://test.com", "https://test.com/"),
                Arguments.of("https://test.com/a", "https://test.com/A"),
                Arguments.of("https://test.com/A", "https://test.com/a"),
                Arguments.of("https://user@test.com", "https://USER@test.com"),
                Arguments.of("https://USER@test.com", "https://user@test.com"),
                Arguments.of("urn:TEST", "urn:test"),
                Arguments.of("urn:test", "urn:TEST"),
                Arguments.of("urn:test/a", "urn:test/A"),
                Arguments.of("urn:test/A", "urn:test/a"),
                Arguments.of("https://test.com?a=1", "https://test.com?A=1"),
                Arguments.of("https://test.com?b=2&a=1", "https://test.com?a=1&b=2")
        );
    }
}
