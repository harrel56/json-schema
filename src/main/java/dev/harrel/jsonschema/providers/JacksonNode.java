package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class JacksonNode implements JsonNode {

    private static final Map<JsonNodeType, SimpleType> TYPE_MAP;
    static {
        Map<JsonNodeType, SimpleType> typeMap = new EnumMap<>(JsonNodeType.class);
        typeMap.put(JsonNodeType.NULL, SimpleType.NULL);
        typeMap.put(JsonNodeType.BOOLEAN, SimpleType.BOOLEAN);
        typeMap.put(JsonNodeType.STRING, SimpleType.STRING);
        typeMap.put(JsonNodeType.NUMBER, SimpleType.NUMBER);
        typeMap.put(JsonNodeType.ARRAY, SimpleType.ARRAY);
        typeMap.put(JsonNodeType.OBJECT, SimpleType.OBJECT);
        TYPE_MAP = Collections.unmodifiableMap(typeMap);
    }

    private final com.fasterxml.jackson.databind.JsonNode node;
    private final String jsonPointer;

    private JacksonNode(com.fasterxml.jackson.databind.JsonNode node, String jsonPointer) {
        this.node = Objects.requireNonNull(node);
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
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
        for (Iterator<com.fasterxml.jackson.databind.JsonNode> iterator = node.elements(); iterator.hasNext(); ) {
            elements.add(new JacksonNode(iterator.next(), jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Map<String, JsonNode> map = new HashMap<>();
        for (Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> iterator = node.fields(); iterator.hasNext(); ) {
            Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry = iterator.next();
            map.put(entry.getKey(), new JacksonNode(entry.getValue(), jsonPointer + "/" + entry.getKey()));
        }
        return map;
    }

    public static final class Factory implements JsonNodeFactory {
        private final ObjectMapper mapper;

        public Factory() {
            this(new ObjectMapper());
        }

        public Factory(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public JacksonNode wrap(Object node) {
            if (node instanceof JacksonNode) {
                return (JacksonNode) node;
            } else if (node instanceof com.fasterxml.jackson.databind.JsonNode) {
                return new JacksonNode((com.fasterxml.jackson.databind.JsonNode) node);
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
