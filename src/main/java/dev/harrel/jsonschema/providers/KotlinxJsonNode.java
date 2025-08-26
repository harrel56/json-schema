package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import kotlinx.serialization.json.*;

import java.math.BigDecimal;
import java.util.*;

public final class KotlinxJsonNode extends AbstractJsonNode<JsonElement> {
    private KotlinxJsonNode(JsonElement node, KotlinxJsonNode parent, Object segment) {
        super(Objects.requireNonNull(node), parent, segment);
    }

    private KotlinxJsonNode(JsonElement node) {
        this(node, null, "");
    }

    @Override
    List<JsonNode> createArray() {
        JsonArray array = (JsonArray) node;
        List<JsonNode> result = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            result.add(new KotlinxJsonNode(array.get(i), this, i));
        }
        return result;
    }

    @Override
    Map<String, JsonNode> createObject() {
        JsonObject object = (JsonObject) node;
        Map<String, JsonNode> result = MapUtil.newHashMap(object.size());
        for (Map.Entry<String, JsonElement> entry : object.getEntries()) {
            result.put(entry.getKey(), new KotlinxJsonNode(entry.getValue(), this, entry.getKey()));
        }
        return result;
    }

    @Override
    SimpleType computeNodeType(JsonElement node) {
        if (node instanceof JsonNull) {
            return SimpleType.NULL;
        } else if (node instanceof JsonObject) {
            return SimpleType.OBJECT;
        } else if (node instanceof JsonArray) {
            return SimpleType.ARRAY;
        } else if (node instanceof JsonPrimitive) {
            JsonPrimitive primitive = (JsonPrimitive) node;
            String content = primitive.getContent();
            if (primitive.isString()) {
                _rawNode = content;
                return SimpleType.STRING;
            }
            if ("true".equals(content)) {
                _rawNode = Boolean.TRUE;
                return SimpleType.BOOLEAN;
            }
            if ("false".equals(content)) {
                _rawNode = Boolean.FALSE;
                return SimpleType.BOOLEAN;
            }
            _rawNode = new BigDecimal(content);
            if (canConvertToInteger((BigDecimal) _rawNode)) {
                return SimpleType.INTEGER;
            } else {
                return SimpleType.NUMBER;
            }
        } else {
            throw new IllegalArgumentException(String.format("Unknown node class [%s]", node.getClass()));
        }
    }

    public static final class Factory implements JsonNodeFactory {
        private final Json json;

        public Factory() {
            this(Json.Default);
        }

        public Factory(Json json) {
            this.json = json;
        }

        @Override
        public KotlinxJsonNode wrap(Object node) {
            if (node instanceof KotlinxJsonNode) {
                KotlinxJsonNode providerNode = (KotlinxJsonNode) node;
                return providerNode.parent == null ? providerNode : new KotlinxJsonNode((providerNode).node);
            } else if (node instanceof JsonElement) {
                return new KotlinxJsonNode((JsonElement) node);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of kotlinx.serialization.json.JsonElement");
            }
        }

        @Override
        public KotlinxJsonNode create(String rawJson) {
            return new KotlinxJsonNode(json.parseToJsonElement(rawJson));
        }
    }
}
