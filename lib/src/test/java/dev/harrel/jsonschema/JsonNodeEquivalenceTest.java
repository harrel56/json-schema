package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.SnakeYamlNode;
import dev.harrel.jsonschema.util.CustomJacksonNode;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.support.ReflectionSupport;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodeEquivalenceTest {
    @TestFactory
    Stream<DynamicNode> equalityWithYamlFactoryAcrossDifferentProviders() {
        JsonNodeFactory yamlFactory = new SnakeYamlNode.Factory();
        return findAllFactories()
                .map(factory -> DynamicContainer.dynamicContainer(
                        factory.getClass().getEnclosingClass().getSimpleName(),
                        dataForEqualityChecks().map(data -> {
                                    JsonNode node1 = yamlFactory.create(data.json1());
                                    JsonNode node2 = factory.create(data.json2());
                                    return DynamicTest.dynamicTest(data.toString(), () -> checkEquality(node1, node2, data.expected()));
                                }
                        )));

    }

    @TestFactory
    Stream<DynamicNode> equalityWithExternalFactoryAcrossDifferentProviders() {
        JsonNodeFactory externalFactory = new CustomJacksonNode.Factory();
        return findAllFactories()
                .map(factory -> DynamicContainer.dynamicContainer(
                        factory.getClass().getEnclosingClass().getSimpleName(),
                        dataForEqualityChecks().map(data -> {
                                    JsonNode node1 = externalFactory.create(data.json1());
                                    JsonNode node2 = factory.create(data.json2());
                                    return DynamicTest.dynamicTest(data.toString(), () -> checkExternalEquality(node1, node2, data.expected()));
                                }
                        )));

    }

    private void checkEquality(JsonNode node1, JsonNode node2, boolean expected) {
        assertThat(JsonNodeUtil.equals(node1, node2)).isEqualTo(expected);
        assertThat(JsonNodeUtil.equals(node2, node1)).isEqualTo(expected);
        assertThat(node1.equals(node2)).isEqualTo(expected);
        assertThat(node2.equals(node1)).isEqualTo(expected);
    }

    private void checkExternalEquality(JsonNode node1, JsonNode node2, boolean expected) {
        assertThat(JsonNodeUtil.equals(node1, node2)).isEqualTo(expected);
        assertThat(JsonNodeUtil.equals(node2, node1)).isEqualTo(expected);
        assertThat(node1.equals(node2)).isFalse();
        assertThat(node2.equals(node1)).isFalse();
    }

    @TestFactory
    Stream<DynamicNode> validationWithYamlFactoryAcrossDifferentProviders() {
        JsonNodeFactory yamlFactory = new SnakeYamlNode.Factory();
        return findAllFactories()
                .map(factory -> DynamicContainer.dynamicContainer(
                        factory.getClass().getEnclosingClass().getSimpleName(),
                        dataForValidation().map(data ->
                                DynamicTest.dynamicTest(data.toString(), () -> {
                                    validate(yamlFactory, factory, data);
                                    validate(factory, yamlFactory, data);
                                })
                        )));

    }
    @TestFactory
    Stream<DynamicNode> validationWithExternalFactoryAcrossDifferentProviders() {
        JsonNodeFactory externalFactory = new CustomJacksonNode.Factory();
        return findAllFactories()
                .map(factory -> DynamicContainer.dynamicContainer(
                        factory.getClass().getEnclosingClass().getSimpleName(),
                        dataForValidation().map(data ->
                                DynamicTest.dynamicTest(data.toString(), () -> {
                                    validate(externalFactory, factory, data);
                                    validate(factory, externalFactory, data);
                                })
                        )));

    }


    private void validate(JsonNodeFactory schemaFactory, JsonNodeFactory instanceFactory, ValidationData data) {
        String schema = """
                {
                  "%s": %s
                }""".formatted(data.keyword(), data.value());

        Validator.Result res = new ValidatorFactory()
                .withJsonNodeFactories(schemaFactory, instanceFactory)
                .validate(schema, data.instance());
        assertThat(res.isValid()).isEqualTo(data.valid());
    }

    private static Stream<JsonNodeFactory> findAllFactories() {
        return ReflectionSupport.findAllClassesInPackage(
                        "dev.harrel.jsonschema.providers",
                        JsonNodeFactory.class::isAssignableFrom,
                        name -> true)
                .stream()
                .map(ReflectionSupport::newInstance)
                .map(JsonNodeFactory.class::cast);
    }

    private record EqualityData(String json1, String json2, boolean expected) {}

    private record ValidationData(String keyword, String value, String instance, boolean valid) {}

    private static Stream<EqualityData> dataForEqualityChecks() {
        return Stream.of(
                new EqualityData("null", "true", false),
                new EqualityData("null", "null", true),
                new EqualityData("true", "true", true),
                new EqualityData("false", "false", true),
                new EqualityData("\"\"", "\"\"", true),
                new EqualityData("\" \"", "\"\"", false),
                new EqualityData("\"\"", "\"\\t\"", false),
                new EqualityData("\"helloThere\"", "\"helloThere\"", true),
                new EqualityData("0", "0", true),
                new EqualityData("0", "-0", true),
                new EqualityData("0", "0.0", true),
                new EqualityData("0", "-0.0", true),
                new EqualityData("12345678900000000", "12345678900000000", true),
                new EqualityData("12345678900000000.0", "12345678900000000.0", true),
                new EqualityData("12345678900000000.1", "12345678900000000.1", true),
                new EqualityData("0.00000000001", "0.00000000001", true),
                new EqualityData("123.321", "123.321", true),
                new EqualityData("123.999999999999", "123.999999999999", true),
                new EqualityData("123.999999999999", "-123.999999999999", false),
                new EqualityData("[]", "[]", true),
                new EqualityData("[  ]", "[]", true),
                new EqualityData("[null]", "[null]", true),
                new EqualityData("[true]", "[false]", false),
                new EqualityData("[\"hi\"]", "[\"hi\"]", true),
                new EqualityData("[0]", "[0.0]", true),
                new EqualityData("[0.0909]", "[0.0909]", true),
                new EqualityData("[[]]", "[[]]", true),
                new EqualityData("[[[]]]", "[[[]]]", true),
                new EqualityData("[[[]]]", "[[]]", false),
                new EqualityData("[1, [null, [], []]]", "[1, [null, [], []]]", true),
                new EqualityData("[1, [[], [], []]]", "[1, [null, [], []]]", false),
                new EqualityData("{}", "{}", true),
                new EqualityData("{}", "[]", false),
                new EqualityData("{\"abc\": null}", "{\"abc\": null}", true),
                new EqualityData("{\"abc\": null}", "{\"abc\": false}", false),
                new EqualityData("{\"abc\": {}}", "{\"abc\": {}}", true),
                new EqualityData("{\"abc\": {}}", "{}", false),
                new EqualityData("{\"abc\": {\"xyz\": []}}", "{\"abc\": {\"xyz\": []}}", true),
                new EqualityData("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", "{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", true),
                new EqualityData("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.1}]}}", "{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", false),
                new EqualityData("{\"abc\": {\"xyz\": [{}, 1, 2, {\"que\": 0.0}]}}", "{\"abc\": {\"xyz\": [{}, 1, 1, {\"que\": 0.0}]}}", false)
        );
    }

    private static Stream<ValidationData> dataForValidation() {
        return Stream.of(
                new ValidationData(Keyword.CONST, "null", "null", true),
                new ValidationData(Keyword.CONST, "null", "\"null\"", false),
                new ValidationData(Keyword.CONST, "{\"abc\": [{}, 1, true]}", "{\"abc\": [{}, 1, true]}", true),
                new ValidationData(Keyword.CONST, "{\"abc\": [{}, 1, true]}", "{\"abc\": [{}, 1, true, true]}", false),
                new ValidationData(Keyword.ENUM, "[]", "null", false),
                new ValidationData(Keyword.ENUM, "[null]", "null", true),
                new ValidationData(Keyword.ENUM, "[null, \"null\", 0, false]", "0.0", true),
                new ValidationData(Keyword.ENUM, "[null, \"null\", 0, false]", "true", false),
                new ValidationData(Keyword.UNIQUE_ITEMS, "false", "[0, 0, 0]", true),
                new ValidationData(Keyword.UNIQUE_ITEMS, "true", "[0, 0.0]", false),
                new ValidationData(Keyword.UNIQUE_ITEMS, "true", "[[{}], [{}]]", false),
                new ValidationData(Keyword.UNIQUE_ITEMS, "true", "[[{\"abc\":[1,2,3]}], [{\"abc\":[1,2,3,4]}]]", true)
        );
    }
}
