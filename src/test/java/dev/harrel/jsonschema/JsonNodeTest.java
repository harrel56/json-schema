package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class JsonNodeTest {

    protected static JsonNodeFactory nodeFactory;

    @Test
    void nullNode() {
        JsonNode node = nodeFactory.create("null");
        assertThat(node).isNotNull();
        assertThat(node.getNodeType()).isEqualTo(SimpleType.NULL);
        assertThat(node.isNull()).isTrue();
        assertThat(node.isBoolean()).isFalse();
        assertThat(node.isString()).isFalse();
        assertThat(node.isInteger()).isFalse();
        assertThat(node.isNumber()).isFalse();
        assertThat(node.isArray()).isFalse();
        assertThat(node.isObject()).isFalse();
        assertThat(node.toPrintableString()).isEqualTo("null");
    }

    @Test
    void booleanNode() {
        JsonNode node = nodeFactory.create("true");
        assertThat(node).isNotNull();
        assertThat(node.getNodeType()).isEqualTo(SimpleType.BOOLEAN);
        assertThat(node.isNull()).isFalse();
        assertThat(node.isBoolean()).isTrue();
        assertThat(node.isString()).isFalse();
        assertThat(node.isInteger()).isFalse();
        assertThat(node.isNumber()).isFalse();
        assertThat(node.isArray()).isFalse();
        assertThat(node.isObject()).isFalse();
        assertThat(node.toPrintableString()).isEqualTo("true");
        assertThat(node.asBoolean()).isTrue();
    }

    @Test
    void stringNode() {
        String value = "~!@#$%6 anything \uD83D\uDCA9";
        JsonNode node = nodeFactory.create("\"" + value + "\"");
        assertThat(node).isNotNull();
        assertThat(node.getNodeType()).isEqualTo(SimpleType.STRING);
        assertThat(node.isNull()).isFalse();
        assertThat(node.isBoolean()).isFalse();
        assertThat(node.isString()).isTrue();
        assertThat(node.isInteger()).isFalse();
        assertThat(node.isNumber()).isFalse();
        assertThat(node.isArray()).isFalse();
        assertThat(node.isObject()).isFalse();
        assertThat(node.toPrintableString()).isEqualTo(value);
        assertThat(node.asString()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-0",
            "0",
            "-1",
            "1",
            "-321123",
            "321123",
            "-5555555555555555555555555555555",
            "5555555555555555555555555555555",
            "-2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222",
            "2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222"
    })
    void integerNode(String value) {
        JsonNode node = nodeFactory.create(value);
        assertThat(node).isNotNull();
        assertThat(node.getNodeType()).isEqualTo(SimpleType.INTEGER);
        assertThat(node.isNull()).isFalse();
        assertThat(node.isBoolean()).isFalse();
        assertThat(node.isString()).isFalse();
        assertThat(node.isInteger()).isTrue();
        assertThat(node.isNumber()).isTrue();
        assertThat(node.isArray()).isFalse();
        assertThat(node.isObject()).isFalse();
        assertThat(node.toPrintableString()).satisfiesAnyOf(
                str -> assertThat(str).isEqualTo(value),
                str -> assertThat(str).isEqualTo(new BigDecimal(value).toString()),
                str -> assertThat(str).isEqualTo(Double.valueOf(value).toString())
        );
        assertThat(node.asInteger()).isEqualTo(new BigDecimal(value).toBigInteger());
        assertThat(node.asNumber()).satisfiesAnyOf(
                num -> assertThat(num).isEqualTo(new BigDecimal(value)),
                num -> assertThat(num).isEqualTo(BigDecimal.valueOf(Double.parseDouble(value)))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-0.0000001",
            "0.0000001",
            "-1.1",
            "1.1",
            "-321123.1",
            "321123.1",
            "-55555555555555555555555555555551.1",
            "55555555555555555555555555555551.1",
            "-2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222.1",
            "2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222.1",
            "-0.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
            "0.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"
    })
    void numberNode(String value) {
        JsonNode node = nodeFactory.create(value);
        assertThat(node).isNotNull();
        assertThat(node.getNodeType()).isEqualTo(SimpleType.NUMBER);
        assertThat(node.isNull()).isFalse();
        assertThat(node.isBoolean()).isFalse();
        assertThat(node.isString()).isFalse();
        assertThat(node.isInteger()).isFalse();
        assertThat(node.isNumber()).isTrue();
        assertThat(node.isArray()).isFalse();
        assertThat(node.isObject()).isFalse();
        assertThat(node.toPrintableString()).satisfiesAnyOf(
                str -> assertThat(str).isEqualTo(value),
                str -> assertThat(str).isEqualTo(new BigDecimal(value).toString()),
                str -> assertThat(str).isEqualTo(Double.valueOf(value).toString())
        );
        assertThat(node.asNumber()).satisfiesAnyOf(
                num -> assertThat(num).isEqualTo(new BigDecimal(value)),
                num -> assertThat(num).isEqualTo(BigDecimal.valueOf(Double.parseDouble(value)))
        );
    }

    @Test
    void arrayNode() {
        String value = "[null, true, \"a\", 1, 1.2]";
        JsonNode node = nodeFactory.create(value);
        assertThat(node).isNotNull();
        assertThat(node.getNodeType()).isEqualTo(SimpleType.ARRAY);
        assertThat(node.isNull()).isFalse();
        assertThat(node.isBoolean()).isFalse();
        assertThat(node.isString()).isFalse();
        assertThat(node.isInteger()).isFalse();
        assertThat(node.isNumber()).isFalse();
        assertThat(node.isArray()).isTrue();
        assertThat(node.isObject()).isFalse();
        assertThat(node.toPrintableString()).isEqualTo("specific array");
        assertThat(node.asArray()).hasSize(5);
    }

    @Test
    void objectNode() {
        String value = "{\"a\": null, \"b\": 1, \"c\": {}}";
        JsonNode node = nodeFactory.create(value);
        assertThat(node).isNotNull();
        assertThat(node.getNodeType()).isEqualTo(SimpleType.OBJECT);
        assertThat(node.isNull()).isFalse();
        assertThat(node.isBoolean()).isFalse();
        assertThat(node.isString()).isFalse();
        assertThat(node.isInteger()).isFalse();
        assertThat(node.isNumber()).isFalse();
        assertThat(node.isArray()).isFalse();
        assertThat(node.isObject()).isTrue();
        assertThat(node.toPrintableString()).isEqualTo("specific object");
        assertThat(node.asObject()).containsKeys("a", "b", "c");
    }

    @Test
    void jsonPointerForNull() {
        JsonNode node = nodeFactory.create("null");
        assertThat(node.getJsonPointer()).isEmpty();
    }

    @Test
    void jsonPointerForBoolean() {
        JsonNode node = nodeFactory.create("false");
        assertThat(node.getJsonPointer()).isEmpty();
    }

    @Test
    void jsonPointerForString() {
        JsonNode node = nodeFactory.create("\"anything\"");
        assertThat(node.getJsonPointer()).isEmpty();
    }

    @Test
    void jsonPointerForInteger() {
        JsonNode node = nodeFactory.create("123");
        assertThat(node.getJsonPointer()).isEmpty();
    }

    @Test
    void jsonPointerForNumber() {
        JsonNode node = nodeFactory.create("123.321");
        assertThat(node.getJsonPointer()).isEmpty();
    }

    @Test
    void jsonPointerForArray() {
        JsonNode node = nodeFactory.create("[[1, 2], []]");
        assertThat(node.getJsonPointer()).isEmpty();
        List<JsonNode> nodes = node.asArray();
        assertThat(nodes.get(0).getJsonPointer()).isEqualTo("/0");
        assertThat(nodes.get(1).getJsonPointer()).isEqualTo("/1");
        assertThat(nodes.get(0).asArray().get(0).getJsonPointer()).isEqualTo("/0/0");
        assertThat(nodes.get(0).asArray().get(1).getJsonPointer()).isEqualTo("/0/1");
    }

    @Test
    void jsonPointerForObject() {
        JsonNode node = nodeFactory.create("{\"a\": [1, {\"b\": 2}]}");
        assertThat(node.getJsonPointer()).isEmpty();
        Map<String, JsonNode> object = node.asObject();
        assertThat(object.get("a").getJsonPointer()).isEqualTo("/a");
        assertThat(object.get("a").asArray().get(0).getJsonPointer()).isEqualTo("/a/0");
        assertThat(object.get("a").asArray().get(1).getJsonPointer()).isEqualTo("/a/1");
        assertThat(object.get("a").asArray().get(1).asObject().get("b").getJsonPointer()).isEqualTo("/a/1/b");
    }

    @Test
    void nullEquals() {
        JsonNode node1 = nodeFactory.create("null");
        JsonNode node2 = nodeFactory.create("null");
        assertThat(node1.isEqualTo(node2)).isTrue();
        assertThat(node2.isEqualTo(node1)).isTrue();

        assertThat(node1.isEqualTo(nodeFactory.create("true"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("\"a\""))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1.1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("[1]"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("{}"))).isFalse();
    }

    @Test
    void booleanEquals() {
        JsonNode node1 = nodeFactory.create("true");
        JsonNode node2 = nodeFactory.create("true");
        assertThat(node1.isEqualTo(node2)).isTrue();
        assertThat(node2.isEqualTo(node1)).isTrue();

        assertThat(node1.isEqualTo(nodeFactory.create("null"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("false"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("\"a\""))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1.1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("[1]"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("{}"))).isFalse();
    }

    @Test
    void stringEquals() {
        JsonNode node1 = nodeFactory.create("\"a\"");
        JsonNode node2 = nodeFactory.create("\"a\"");
        assertThat(node1.isEqualTo(node2)).isTrue();
        assertThat(node2.isEqualTo(node1)).isTrue();

        assertThat(node1.isEqualTo(nodeFactory.create("null"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("false"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("\"\""))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1.1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("[1]"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("{}"))).isFalse();
    }

    @Test
    void integerEquals() {
        JsonNode node1 = nodeFactory.create("123");
        JsonNode node2 = nodeFactory.create("123");
        assertThat(node1.isEqualTo(node2)).isTrue();
        assertThat(node2.isEqualTo(node1)).isTrue();
        assertThat(node1.isEqualTo(nodeFactory.create("123.0"))).isTrue();

        assertThat(node1.isEqualTo(nodeFactory.create("null"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("false"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("\"a\""))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("123.01"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("[1]"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("{}"))).isFalse();
    }

    @Test
    void numberEquals() {
        JsonNode node1 = nodeFactory.create("1.01");
        JsonNode node2 = nodeFactory.create("1.01");
        assertThat(node1.isEqualTo(node2)).isTrue();
        assertThat(node2.isEqualTo(node1)).isTrue();

        assertThat(node1.isEqualTo(nodeFactory.create("null"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("false"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("\"a\""))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("123.01"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("[1]"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("{}"))).isFalse();
    }

    @Test
    void arrayEquals() {
        String value = "[null, true, \"a\", 1, 1.2]";
        JsonNode node1 = nodeFactory.create(value);
        JsonNode node2 = nodeFactory.create(value);
        assertThat(node1.isEqualTo(node2)).isTrue();
        assertThat(node2.isEqualTo(node1)).isTrue();

        assertThat(node1.isEqualTo(nodeFactory.create("null"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("false"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("\"a\""))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("123.01"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("[1]"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("{}"))).isFalse();

        assertThat(node1.isEqualTo(nodeFactory.create("[true, null, \"a\", 1, 1.2]"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("[[true, null, \"a\", 1, 1.2]]"))).isFalse();
    }

    @Test
    void objectEquals() {
        String value = "{\"a\": [1, {\"b\": 2}], \"b\": 1}";
        JsonNode node1 = nodeFactory.create(value);
        JsonNode node2 = nodeFactory.create(value);
        assertThat(node1.isEqualTo(node2)).isTrue();
        assertThat(node2.isEqualTo(node1)).isTrue();
        assertThat(node1.isEqualTo(nodeFactory.create("{\"a\": [1, {\"b\": 2.0}], \"b\": 1}"))).isTrue();
        assertThat(node1.isEqualTo(nodeFactory.create("{\"b\": 1, \"a\": [1, {\"b\": 2}]}"))).isTrue();

        assertThat(node1.isEqualTo(nodeFactory.create("null"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("false"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("\"a\""))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("1"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("123.01"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("[1]"))).isFalse();
        assertThat(node1.isEqualTo(nodeFactory.create("{\"a\": []}"))).isFalse();

        assertThat(node1.isEqualTo(nodeFactory.create("{\"a\": [1, {\"b\": 2.1}], \"b\": 1}"))).isFalse();
    }
}
