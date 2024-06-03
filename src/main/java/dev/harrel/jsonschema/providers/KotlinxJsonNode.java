package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import kotlinx.serialization.json.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class KotlinxJsonNode extends AbstractJsonNode<JsonElement> {
    private BigDecimal asNumber;

    private KotlinxJsonNode(JsonElement node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    private KotlinxJsonNode(JsonElement node) {
        this(node, "");
    }

    @Override
    public boolean asBoolean() {
        return Boolean.parseBoolean(((JsonPrimitive) node).getContent());
    }

    @Override
    public String asString() {
        return ((JsonPrimitive) node).getContent();
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
        JsonArray array = (JsonArray) node;
        List<JsonNode> result = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            result.add(new KotlinxJsonNode(array.get(i), jsonPointer + "/" + i));
        }
        return result;
    }

    @Override
    Map<String, JsonNode> createObject() {
        JsonObject object = (JsonObject) node;
        Map<String, JsonNode> result = MapUtil.newHashMap(object.size());
        for (Map.Entry<String, JsonElement> entry : object.getEntries()) {
            result.put(entry.getKey(), new KotlinxJsonNode(entry.getValue(), this.jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
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
            if (primitive.isString()) {
                return SimpleType.STRING;
            }
            String content = primitive.getContent();
            if ("true".equals(content) || "false".equals(content)) {
                return SimpleType.BOOLEAN;
            }
            asNumber = new BigDecimal(content);
            if (canConvertToInteger(asNumber)) {
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
                return new KotlinxJsonNode(((KotlinxJsonNode) node).node);
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
