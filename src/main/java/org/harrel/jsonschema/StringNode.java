package org.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class StringNode implements JsonNode {
    private final String value;
    private final String jsonPointer;

    public StringNode(String value, String jsonPointer) {
        this.value = value;
        this.jsonPointer = jsonPointer;
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
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
