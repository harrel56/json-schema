package org.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
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
    public Iterable<JsonNode> asArray() {
        Iterator<com.fasterxml.jackson.databind.JsonNode> nodeIterator = node.elements();
        Iterator<JsonNode> iterator = new Iterator<>() {
            private int idx;

            @Override
            public boolean hasNext() {
                return nodeIterator.hasNext();
            }

            @Override
            public JsonNode next() {
                return new JacksonNode(nodeIterator.next(), jsonPointer + "/" + idx++);
            }
        };
        return () -> iterator;
    }

    @Override
    public Iterable<Map.Entry<String, JsonNode>> asObject() {
        Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> nodeIterator = node.fields();
        Iterator<Map.Entry<String, JsonNode>> iterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return nodeIterator.hasNext();
            }

            @Override
            public Map.Entry<String, JsonNode> next() {
                Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> next = nodeIterator.next();
                return Map.entry(next.getKey(), new JacksonNode(next.getValue(), jsonPointer + "/" + next.getKey()));
            }
        };
        return () -> iterator;
    }

    @Override
    public boolean isEqualTo(JsonNode other) {
        return node.equals(((JacksonNode) other).node);
    }
}
