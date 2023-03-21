package org.harrel.jsonschema.providers;

import com.google.gson.JsonElement;
import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class GsonNode implements JsonNode {
    private final JsonElement node;
    private final String jsonPointer;

    private GsonNode(JsonElement node, String jsonPointer) {
        this.node = node;
        this.jsonPointer = jsonPointer;
    }

    public GsonNode(JsonElement node) {
        this(node, "");
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public SimpleType getNodeType() {
        return null;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public String asString() {
        return null;
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
        return null;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        return null;
    }
}
