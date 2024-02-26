package dev.harrel.jsonschema.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class GsonNode extends AbstractJsonNode<JsonElement> {
    private GsonNode(JsonElement node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    public GsonNode(JsonElement node) {
        this(node, "");
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
        if (asArray != null) {
            return asArray;
        }
        List<JsonElement> elements = node.getAsJsonArray().asList();
        List<JsonNode> result = new ArrayList<>(elements.size());
        for (int i = 0; i < elements.size(); i++) {
            result.add(new GsonNode(elements.get(i), jsonPointer + "/" + i));
        }
        this.asArray = result;
        return result;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        if (asObject != null) {
            return asObject;
        }
        Set<Map.Entry<String, JsonElement>> objectMap = node.getAsJsonObject().entrySet();
        Map<String, JsonNode> result = MapUtil.newHashMap(objectMap.size());
        for (Map.Entry<String, JsonElement> entry : objectMap) {
            result.put(entry.getKey(), new GsonNode(entry.getValue(), this.jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        this.asObject = result;
        return result;
    }

    @Override
    public String toPrintableString() {
        if (isNull()) {
            return "null";
        } else {
            return super.toPrintableString();
        }
    }

    @Override
    SimpleType computeNodeType(JsonElement node) {
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
            if (node instanceof GsonNode) {
                return (GsonNode) node;
            } else if (node instanceof JsonElement) {
                return new GsonNode((JsonElement) node);
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
