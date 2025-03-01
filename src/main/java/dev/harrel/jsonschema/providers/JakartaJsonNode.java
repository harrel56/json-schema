package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import jakarta.json.*;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;

public final class JakartaJsonNode extends AbstractJsonNode<JsonValue> {
    private JakartaJsonNode(JsonValue node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    public JakartaJsonNode(JsonValue node) {
        this(node, "");
    }

    @Override
    List<JsonNode> createArray() {
        JsonArray jsonArray = node.asJsonArray();
        List<JsonNode> result = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            result.add(new JakartaJsonNode(jsonArray.get(i), jsonPointer + "/" + i));
        }
        return result;
    }

    @Override
    Map<String, JsonNode> createObject() {
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
                rawNode = ((JsonString) node).getString();
                return SimpleType.STRING;
            case TRUE:
                rawNode = Boolean.TRUE;
                return SimpleType.BOOLEAN;
            case FALSE:
                rawNode = Boolean.FALSE;
                return SimpleType.BOOLEAN;
            case NUMBER:
                rawNode = ((JsonNumber) node).bigDecimalValue();
                if (canConvertToInteger((BigDecimal) rawNode)) {
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
                return new JakartaJsonNode(((JakartaJsonNode) node).node);
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
