package dev.harrel.jsonschema.providers;

import com.google.gson.JsonArray;
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
    private BigDecimal asNumber;

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
        return asNumber.toBigInteger();
    }

    @Override
    public BigDecimal asNumber() {
        return asNumber;
    }

    @Override
    List<JsonNode> createArray() {
        JsonArray array = node.getAsJsonArray();
        List<JsonNode> result = new ArrayList<>(array.size());
        int i = 0;
        for (JsonElement elem : array) {
            result.add(new GsonNode(elem, jsonPointer + "/" + i++));
        }
        return result;
    }

    @Override
    Map<String, JsonNode> createObject() {
        Set<Map.Entry<String, JsonElement>> objectMap = node.getAsJsonObject().entrySet();
        Map<String, JsonNode> result = MapUtil.newHashMap(objectMap.size());
        for (Map.Entry<String, JsonElement> entry : objectMap) {
            result.put(entry.getKey(), new GsonNode(entry.getValue(), this.jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
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
                asNumber = jsonPrimitive.getAsBigDecimal();
                if (canConvertToInteger(asNumber)) {
                    return SimpleType.INTEGER;
                } else {
                    return SimpleType.NUMBER;
                }
            }
        }
    }

    /* Using deprecated API to support older versions as well */
    @SuppressWarnings("deprecation")
    public static final class Factory implements JsonNodeFactory {
        private final JsonParser jsonParser = new JsonParser();

        @Override
        public GsonNode wrap(Object node) {
            if (node instanceof GsonNode) {
                return new GsonNode(((GsonNode) node).node);
            } else if (node instanceof JsonElement) {
                return new GsonNode((JsonElement) node);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of com.google.gson.JsonElement");
            }
        }

        @Override
        public GsonNode create(String rawJson) {
            return new GsonNode(jsonParser.parse(rawJson));
        }
    }
}
