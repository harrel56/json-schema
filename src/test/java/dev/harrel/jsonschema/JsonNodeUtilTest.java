package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.SnakeYamlNode;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.support.ReflectionSupport;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodeUtilTest {
    private final JsonNodeFactory baseFactory = new SnakeYamlNode.Factory();

    @TestFactory
    Stream<DynamicNode> shouldProperlyCheckForEqualityCrossProviders() {
        List<JsonNodeFactory> factories = ReflectionSupport.findAllClassesInPackage(
                        "dev.harrel.jsonschema.providers",
                        JsonNodeFactory.class::isAssignableFrom,
                        name -> true)
                .stream()
                .map(ReflectionSupport::newInstance)
                .map(JsonNodeFactory.class::cast)
                .toList();
        return factories.stream().map(factory -> DynamicContainer.dynamicContainer(factory.getClass().getEnclosingClass().getSimpleName(),
                dataForEqualityChecks().map(args -> {
                            JsonNode node1 = baseFactory.create((String) args.get()[0]);
                            JsonNode node2 = factory.create((String) args.get()[1]);
                            return DynamicTest.dynamicTest(Arrays.toString(args.get()), () -> checkEquality(node1, node2, (boolean) args.get()[2]));
                        }
                )));

    }

    private void checkEquality(JsonNode node1, JsonNode node2, boolean expected) {
        assertThat(JsonNodeUtil.equals(node1, node2)).isEqualTo(expected);
        assertThat(JsonNodeUtil.equals(node2, node1)).isEqualTo(expected);
        assertThat(node1.equals(node2)).isEqualTo(expected);
        assertThat(node2.equals(node1)).isEqualTo(expected);
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
                Arguments.of("{\"abc\": null}", "{\"abc\": false}", false),
                Arguments.of("{\"abc\": {}}", "{\"abc\": {}}", true),
                Arguments.of("{\"abc\": {}}", "{}", false),
                Arguments.of("{\"abc\": {\"xyz\": []}}", "{\"abc\": {\"xyz\": []}}", true),
                Arguments.of("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", "{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", true),
                Arguments.of("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.1}]}}", "{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", false),
                Arguments.of("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", "{\"abc\": {\"xyz\": [{}, 1, 1, {\"que\": 0.0}]}}", false)
        );
    }
}