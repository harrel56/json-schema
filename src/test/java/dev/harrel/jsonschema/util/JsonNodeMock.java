package dev.harrel.jsonschema.util;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class JsonNodeMock implements JsonNode {
    @Override
    public String getJsonPointer() {
        return "";
    }

    @Override
    public SimpleType getNodeType() {
        return SimpleType.BOOLEAN;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public String asString() {
        return "a";
    }

    @Override
    public BigInteger asInteger() {
        return BigInteger.valueOf(1);
    }

    @Override
    public BigDecimal asNumber() {
        return BigDecimal.valueOf(1);
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
