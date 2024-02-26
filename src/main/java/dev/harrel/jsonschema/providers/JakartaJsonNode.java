package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import jakarta.json.*;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class JakartaJsonNode extends AbstractJsonNode<JsonValue> {
    private JakartaJsonNode(JsonValue node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    public JakartaJsonNode(JsonValue node) {
        this(node, "");
    }

    @Override
    public boolean asBoolean() {
        return node.getValueType() == JsonValue.ValueType.TRUE;
    }

    @Override
    public String asString() {
        return node instanceof JsonString ? ((JsonString) node).getChars().toString() : node.toString();
    }

    @Override
    public BigInteger asInteger() {
        return ((JsonNumber) node).bigIntegerValue();
    }

    @Override
    public BigDecimal asNumber() {
        return ((JsonNumber) node).bigDecimalValue();
    }

    @Override
    public List<JsonNode> asArray() {
        JsonArray jsonArray = node.asJsonArray();
        List<JsonNode> result = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            result.add(new JakartaJsonNode(jsonArray.get(i), jsonPointer + "/" + i));
        }
        return result;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Set<Map.Entry<String, JsonValue>> objectMap = node.asJsonObject().entrySet();
        Map<String, JsonNode> result = MapUtil.newHashMap(objectMap.size());
        for (Map.Entry<String, JsonValue> entry : objectMap) {
            result.put(entry.getKey(), new JakartaJsonNode(entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        return result;
    }

    @Override
    SimpleType computeNodeType(JsonValue node) {
        switch (node.getValueType()) {
            case NULL:
                return SimpleType.NULL;
            case ARRAY:
                return SimpleType.ARRAY;
            case OBJECT:
                return SimpleType.OBJECT;
            case STRING:
                return SimpleType.STRING;
            case TRUE:
            case FALSE:
                return SimpleType.BOOLEAN;
            case NUMBER:
                JsonNumber jsonNumber = (JsonNumber) node;
                if (jsonNumber.isIntegral() || jsonNumber.bigDecimalValue().stripTrailingZeros().scale() <= 0) {
                    return SimpleType.INTEGER;
                } else {
                    return SimpleType.NUMBER;
                }
            default:
                throw new IllegalArgumentException("Unknown node type=" + node.getValueType());
        }
    }

    public static final class Factory implements JsonNodeFactory {
        private final JsonParserFactory parserFactory;

        public Factory() {
            this(Json.createParserFactory(Collections.emptyMap()));
        }

        public Factory(JsonParserFactory parserFactory) {
            this.parserFactory = parserFactory;
        }

        @Override
        public JakartaJsonNode wrap(Object node) {
            if (node instanceof JakartaJsonNode) {
                return (JakartaJsonNode) node;
            } else if (node instanceof JsonValue) {
                return new JakartaJsonNode((JsonValue) node);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of jakarta.json.JsonValue");
            }
        }

        @Override
        public JakartaJsonNode create(String rawJson) {
            try (JsonParser parser = parserFactory.createParser(new StringReader(rawJson))) {
                parser.next();
                return new JakartaJsonNode(parser.getValue());
            }
        }
    }
}
