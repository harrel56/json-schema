package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public final class JacksonNode extends AbstractJsonNode<com.fasterxml.jackson.databind.JsonNode> {
    private JacksonNode(com.fasterxml.jackson.databind.JsonNode node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    public JacksonNode(com.fasterxml.jackson.databind.JsonNode node) {
        this(node, "");
    }

    @Override
    List<JsonNode> createArray() {
        List<JsonNode> elements = new ArrayList<>(node.size());
        for (Iterator<com.fasterxml.jackson.databind.JsonNode> iterator = node.elements(); iterator.hasNext(); ) {
            elements.add(new JacksonNode(iterator.next(), jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    Map<String, JsonNode> createObject() {
        Map<String, JsonNode> map = MapUtil.newHashMap(node.size());
        for (Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> iterator = node.fields(); iterator.hasNext(); ) {
            Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry = iterator.next();
            map.put(entry.getKey(), new JacksonNode(entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        return map;
    }

    @Override
    SimpleType computeNodeType(com.fasterxml.jackson.databind.JsonNode node) {
        switch (node.getNodeType()) {
            case NULL:
                return SimpleType.NULL;
            case BOOLEAN:
                rawNode = node.asBoolean();
                return SimpleType.BOOLEAN;
            case STRING:
                rawNode = node.asText();
                return SimpleType.STRING;
            case NUMBER:
                rawNode = node.decimalValue();
                if (canConvertToInteger((BigDecimal) rawNode)) {
                    return SimpleType.INTEGER;
                } else {
                    return SimpleType.NUMBER;
                }
            case ARRAY:
                return SimpleType.ARRAY;
            case OBJECT:
                return SimpleType.OBJECT;
            default:
                throw new IllegalArgumentException(String.format("Unknown node type [%s]", node.getNodeType()));
        }
    }

    public static final class Factory implements JsonNodeFactory {
        private final ObjectMapper mapper;

        public Factory() {
            this(new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
        }

        public Factory(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public JacksonNode wrap(Object node) {
            if (node instanceof JacksonNode) {
                JacksonNode providerNode = (JacksonNode) node;
                return providerNode.jsonPointer.isEmpty() ? providerNode : new JacksonNode((providerNode).node);
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
                throw new IllegalArgumentException(e);
            }
        }
    }
}
