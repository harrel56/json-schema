package org.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public final class StringNode implements JsonNode {
    private final String value;
    private final String jsonPointer;

    public StringNode(String value, String jsonPointer) {
        this.value = value;
        this.jsonPointer = jsonPointer;
    }

    @Override
    public SimpleType getNodeType() {
        return SimpleType.STRING;
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public BigInteger asInteger() {
        return null;
    }

    @Override
    public BigDecimal asNumber() {
        return null;
    }

    @Override
    public List<JsonNode> asArray() {
        return List.of();
    }

    @Override
    public Map<String, JsonNode> asObject() {
        return Map.of();
    }
}
