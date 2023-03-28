package org.harrel.jsonschema.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.JsonNodeFactory;
import org.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class GsonNode implements JsonNode {
    private final JsonElement node;
    private final String jsonPointer;
    private final SimpleType nodeType;

    private GsonNode(JsonElement node, String jsonPointer) {
        this.node = node;
        this.jsonPointer = jsonPointer;
        this.nodeType = computeNodeType();
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
        return nodeType;
    }

    @Override
    public boolean isInteger() {
        return JsonNode.super.isInteger();
    }

    @Override
    public boolean isNumber() {
        return JsonNode.super.isNumber();
    }

    @Override
    public boolean asBoolean() {
        return node.getAsBoolean();
    }

    @Override
    public String asString() {
        return node.getAsString();
    }

    @Override
    public BigInteger asInteger() {
        return node.getAsBigDecimal().toBigInteger();
    }

    @Override
    public BigDecimal asNumber() {
        return node.getAsBigDecimal();
    }

    @Override
    public List<JsonNode> asArray() {
        List<JsonElement> elements = node.getAsJsonArray().asList();
        List<JsonNode> result = new ArrayList<>(elements.size());
        for (int i = 0; i < elements.size(); i++) {
            result.add(new GsonNode(elements.get(i), jsonPointer + "/" + i));
        }
        return result;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Set<Map.Entry<String, JsonElement>> objectMap = node.getAsJsonObject().entrySet();
        Map<String, JsonNode> result = new HashMap<>(objectMap.size());
        for (var entry : objectMap) {
            result.put(entry.getKey(), new GsonNode(entry.getValue(), jsonPointer + "/" + entry.getKey()));
        }
        return result;
    }

    @Override
    public String toPrintableString() {
        if (isNull()) {
            return "null";
        } else {
            return JsonNode.super.toPrintableString();
        }
    }

    public SimpleType computeNodeType() {
        if (node.isJsonNull()) {
            return SimpleType.NULL;
        } else if (node.isJsonArray()) {
            return SimpleType.ARRAY;
        } else if (node.isJsonObject()) {
            return SimpleType.OBJECT;
        } else {
            JsonPrimitive jsonPrimitive = node.getAsJsonPrimitive();
            if (jsonPrimitive.isBoolean()) {
                return SimpleType.BOOLEAN;
            } else if (jsonPrimitive.isString()) {
                return SimpleType.STRING;
            } else {
                String asString = jsonPrimitive.getAsString();
                if (!asString.contains(".") || asString.split("\\.")[1].matches("^0*$")) {
                    return SimpleType.INTEGER;
                } else {
                    return SimpleType.NUMBER;
                }
            }
        }
    }

    public static final class Factory implements JsonNodeFactory {
        @Override
        public GsonNode wrap(Object node) {
            if (node instanceof JsonElement vendorNode) {
                return new GsonNode(vendorNode);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of com.google.gson.JsonElement");
            }
        }

        @Override
        public GsonNode create(String rawJson) {
            return new GsonNode(JsonParser.parseString(rawJson));
        }
    }
}
