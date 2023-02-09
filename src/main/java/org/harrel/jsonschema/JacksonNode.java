package org.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonNode implements JsonNode {

    private final com.fasterxml.jackson.databind.JsonNode node;
    private final String jsonPointer;

    public JacksonNode(com.fasterxml.jackson.databind.JsonNode node) {
        this(node, "");
    }

    private JacksonNode(com.fasterxml.jackson.databind.JsonNode node, String jsonPointer) {
        this.node = node;
        this.jsonPointer = jsonPointer;
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public boolean isNull() {
        return node.isNull();
    }

    @Override
    public boolean isBoolean() {
        return node.isBoolean();
    }

    @Override
    public boolean isString() {
        return node.isTextual();
    }

    @Override
    public boolean isInteger() {
        return node.canConvertToExactIntegral();
    }

    @Override
    public boolean isNumber() {
        return node.isNumber();
    }

    @Override
    public boolean isArray() {
        return node.isArray();
    }

    @Override
    public boolean isObject() {
        return node.isObject();
    }

    @Override
    public boolean asBoolean() {
        return node.asBoolean();
    }

    @Override
    public String asString() {
        return node.asText();
    }

    @Override
    public BigInteger asInteger() {
        return node.bigIntegerValue();
    }

    @Override
    public BigDecimal asNumber() {
        return node.decimalValue();
    }

    @Override
    public List<JsonNode> asArray() {
        List<JsonNode> elements = new ArrayList<>();
        for (var iterator = node.elements(); iterator.hasNext(); ) {
            elements.add(new JacksonNode(iterator.next(), jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Map<String, JsonNode> map = new HashMap<>();
        for (var iterator = node.fields(); iterator.hasNext(); ) {
            var entry = iterator.next();
            map.put(entry.getKey(), new JacksonNode(entry.getValue(), jsonPointer + "/" + entry.getKey()));
        }
        return map;
    }

    @Override
    public boolean isEqualTo(JsonNode other) {
        return node.equals(((JacksonNode) other).node);
    }
}
