package org.harrel.jsonschema.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.JsonNodeFactory;
import org.harrel.jsonschema.SimpleType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonNode implements JsonNode {

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

    private JacksonNode(com.fasterxml.jackson.databind.JsonNode node, String jsonPointer) {
        this.node = node;
        this.jsonPointer = jsonPointer;
    }

    public JacksonNode(com.fasterxml.jackson.databind.JsonNode node) {
        this(node, "");
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public SimpleType getNodeType() {
        SimpleType type = TYPE_MAP.get(node.getNodeType());
        if (node.canConvertToExactIntegral()) {
            return SimpleType.INTEGER;
        } else {
            return type;
        }
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
            elements.add(new JacksonNode(iterator.next(), jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Map<String, JsonNode> map = new HashMap<>();
        for (var iterator = node.fields(); iterator.hasNext(); ) {
            var entry = iterator.next();
            map.put(entry.getKey(), new JacksonNode(entry.getValue(), jsonPointer + "/" + entry.getKey()));
        }
        return map;
    }

    public static class Factory implements JsonNodeFactory {
        private final ObjectMapper mapper;

        public Factory() {
            this(new ObjectMapper());
        }

        public Factory(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public JacksonNode wrap(Object node) {
            if (node instanceof com.fasterxml.jackson.databind.JsonNode vendorNode) {
                return new JacksonNode(vendorNode);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of com.fasterxml.jackson.databind.JsonNode");
            }
        }

        @Override
        public JacksonNode create(String rawJson) {
            try {
                return new JacksonNode(mapper.readTree(rawJson));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
