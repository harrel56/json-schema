package dev.harrel.jsonschema.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomJacksonNode implements JsonNode {

    private static final Map<JsonNodeType, SimpleType> TYPE_MAP = Map.of(
            JsonNodeType.NULL, SimpleType.NULL,
            JsonNodeType.BOOLEAN, SimpleType.BOOLEAN,
            JsonNodeType.STRING, SimpleType.STRING,
            JsonNodeType.NUMBER, SimpleType.NUMBER,
            JsonNodeType.ARRAY, SimpleType.ARRAY,
            JsonNodeType.OBJECT, SimpleType.OBJECT
    );

    private final com.fasterxml.jackson.databind.JsonNode node;
    private final String jsonPointer;

    private CustomJacksonNode(com.fasterxml.jackson.databind.JsonNode node, String jsonPointer) {
        this.node = node;
        this.jsonPointer = jsonPointer;
    }

    public CustomJacksonNode(com.fasterxml.jackson.databind.JsonNode node) {
        this(node, "");
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public SimpleType getNodeType() {
        SimpleType type = TYPE_MAP.get(node.getNodeType());
        if (type == SimpleType.NUMBER) {
            return isInteger() ? SimpleType.INTEGER : SimpleType.NUMBER;
        } else {
            return type;
        }
    }

    @Override
    public boolean isInteger() {
        return node.canConvertToExactIntegral();
    }

    @Override
    public boolean isNumber() {
        return node.isNumber();
    }

    @Override
    public boolean asBoolean() {
        return node.asBoolean();
    }

    @Override
    public String asString() {
        return node.asText();
    }

    @Override
    public BigInteger asInteger() {
        return node.bigIntegerValue();
    }

    @Override
    public BigDecimal asNumber() {
        return node.decimalValue();
    }

    @Override
    public List<JsonNode> asArray() {
        List<JsonNode> elements = new ArrayList<>();
        for (var iterator = node.elements(); iterator.hasNext(); ) {
            elements.add(new CustomJacksonNode(iterator.next(), jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Map<String, JsonNode> map = new HashMap<>();
        for (var iterator = node.fields(); iterator.hasNext(); ) {
            var entry = iterator.next();
            map.put(entry.getKey(), new CustomJacksonNode(entry.getValue(), jsonPointer + "/" + entry.getKey()));
        }
        return map;
    }

    public static class Factory implements JsonNodeFactory {
        private final ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        @Override
        public JsonNode wrap(Object node) {
            if (node instanceof com.fasterxml.jackson.databind.JsonNode vendorNode) {
                return new CustomJacksonNode(vendorNode);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of com.fasterxml.jackson.databind.JsonNode");
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            try {
                return new CustomJacksonNode(mapper.readTree(rawJson));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
