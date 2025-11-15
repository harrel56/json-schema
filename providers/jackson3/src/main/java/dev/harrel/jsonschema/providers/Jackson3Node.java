package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.*;

public final class Jackson3Node extends AbstractJsonNode<tools.jackson.databind.JsonNode> {
    private Jackson3Node(tools.jackson.databind.JsonNode node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    public Jackson3Node(tools.jackson.databind.JsonNode node) {
        this(node, "");
    }

    @Override
    List<JsonNode> createArray() {
        List<JsonNode> elements = new ArrayList<>(node.size());
        for (tools.jackson.databind.JsonNode jsonNode : node.values()) {
            elements.add(new Jackson3Node(jsonNode, jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    Map<String, JsonNode> createObject() {
        Map<String, JsonNode> map = MapUtil.newHashMap(node.size());
        for (Map.Entry<String, tools.jackson.databind.JsonNode> entry : node.properties()) {
            map.put(entry.getKey(), new Jackson3Node(entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        return map;
    }

    @Override
    SimpleType computeNodeType(tools.jackson.databind.JsonNode node) {
        switch (node.getNodeType()) {
            case NULL:
                return SimpleType.NULL;
            case BOOLEAN:
                rawNode = node.asBoolean();
                return SimpleType.BOOLEAN;
            case STRING:
                rawNode = node.asString();
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
            this(JsonMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build());
        }

        public Factory(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public Jackson3Node wrap(Object node) {
            if (node instanceof Jackson3Node providerNode) {
                return providerNode.jsonPointer.isEmpty() ? providerNode : new Jackson3Node(providerNode.node);
            } else if (node instanceof tools.jackson.databind.JsonNode providerNode) {
                return new Jackson3Node(providerNode);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of tools.jackson.databind.JsonNode");
            }
        }

        @Override
        public Jackson3Node create(String rawJson) {
            return new Jackson3Node(mapper.readTree(rawJson));
        }
    }
}
