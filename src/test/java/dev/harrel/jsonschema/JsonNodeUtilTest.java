package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import dev.harrel.jsonschema.providers.OrgJsonNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodeUtilTest {
    private final JsonNodeFactory factory1 = new JacksonNode.Factory();
    private final JsonNodeFactory factory2 = new OrgJsonNode.Factory();

    @ParameterizedTest
    @MethodSource("dataForEqualityChecks")
    void equalityChecks(String json1, String json2, boolean expected) {
        JsonNode node1 = factory1.create(json1);
        JsonNode node2 = factory2.create(json2);

        assertThat(JsonNodeUtil.equals(node1, node2)).isEqualTo(expected);
        assertThat(JsonNodeUtil.equals(node2, node1)).isEqualTo(expected);
    }

    private static Stream<Arguments> dataForEqualityChecks() {
        return Stream.of(
                Arguments.of("null", "true", false),
                Arguments.of("null", "null", true),
                Arguments.of("true", "true", true),
                Arguments.of("false", "false", true),
                Arguments.of("\"\"", "\"\"", true),
                Arguments.of("\" \"", "\"\"", false),
                Arguments.of("\"\"", "\"\\t\"", false),
                Arguments.of("\"helloThere\"", "\"helloThere\"", true),
                Arguments.of("0", "0", true),
                Arguments.of("0", "-0", true),
                Arguments.of("0", "0.0", true),
                Arguments.of("0", "-0.0", true),
                Arguments.of("12345678900000000", "12345678900000000", true),
                Arguments.of("12345678900000000.0", "12345678900000000.0", true),
                Arguments.of("12345678900000000.1", "12345678900000000.1", true),
                Arguments.of("0.00000000001", "0.00000000001", true),
                Arguments.of("123.321", "123.321", true),
                Arguments.of("123.999999999999", "123.999999999999", true),
                Arguments.of("123.999999999999", "-123.999999999999", false),
                Arguments.of("[]", "[]", true),
                Arguments.of("[  ]", "[]", true),
                Arguments.of("[null]", "[null]", true),
                Arguments.of("[true]", "[false]", false),
                Arguments.of("[\"hi\"]", "[\"hi\"]", true),
                Arguments.of("[0]", "[0.0]", true),
                Arguments.of("[0.0909]", "[0.0909]", true),
                Arguments.of("[[]]", "[[]]", true),
                Arguments.of("[[[]]]", "[[[]]]", true),
                Arguments.of("[[[]]]", "[[]]", false),
                Arguments.of("[1, [null, [], []]]", "[1, [null, [], []]]", true),
                Arguments.of("[1, [[], [], []]]", "[1, [null, [], []]]", false),
                Arguments.of("{}", "{}", true),
                Arguments.of("{}", "[]", false),
                Arguments.of("{\"abc\": null}", "{\"abc\": null}", true),
                Arguments.of("{\"abc\": {}}", "{\"abc\": {}}", true),
                Arguments.of("{\"abc\": {}}", "{}", false),
                Arguments.of("{\"abc\": {\"xyz\": []}}", "{\"abc\": {\"xyz\": []}}", true),
                Arguments.of("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", "{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", true),
                Arguments.of("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.1}]}}", "{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", false),
                Arguments.of("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", "{\"abc\": {\"xyz\": [{}, 1, 1, {\"que\": 0.0}]}}", false)
        );
    }
}