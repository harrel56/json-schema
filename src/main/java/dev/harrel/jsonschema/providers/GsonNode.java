package dev.harrel.jsonschema.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.util.*;

public final class GsonNode extends AbstractJsonNode<JsonElement> {
    private GsonNode(JsonElement node, GsonNode parent, Object segment) {
        super(Objects.requireNonNull(node), parent, segment);
    }

    public GsonNode(JsonElement node) {
        this(node, null, "");
    }

    @Override
    List<JsonNode> createArray() {
        JsonArray array = node.getAsJsonArray();
        List<JsonNode> result = new ArrayList<>(array.size());
        int i = 0;
        for (JsonElement elem : array) {
            result.add(new GsonNode(elem, this, i++));
        }
        return result;
    }

    @Override
    Map<String, JsonNode> createObject() {
        Set<Map.Entry<String, JsonElement>> objectMap = node.getAsJsonObject().entrySet();
        Map<String, JsonNode> result = MapUtil.newHashMap(objectMap.size());
        for (Map.Entry<String, JsonElement> entry : objectMap) {
            result.put(entry.getKey(), new GsonNode(entry.getValue(), this, entry.getKey()));
        }
        return result;
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
                _rawNode = node.getAsBoolean();
                return SimpleType.BOOLEAN;
            } else if (jsonPrimitive.isString()) {
                _rawNode = node.getAsString();
                return SimpleType.STRING;
            } else {
                _rawNode = jsonPrimitive.getAsBigDecimal();
                if (canConvertToInteger((BigDecimal) _rawNode)) {
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
                GsonNode providerNode = (GsonNode) node;
                return providerNode.parent == null ? providerNode : new GsonNode((providerNode).node);
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
