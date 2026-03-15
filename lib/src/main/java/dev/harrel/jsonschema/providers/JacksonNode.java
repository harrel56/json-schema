package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.internal.AbstractJsonNode;
import dev.harrel.jsonschema.internal.GenericNode;

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
    protected List<JsonNode> createArray() {
        List<JsonNode> elements = new ArrayList<>(node.size());
        for (Iterator<com.fasterxml.jackson.databind.JsonNode> iterator = node.elements(); iterator.hasNext(); ) {
            elements.add(new JacksonNode(iterator.next(), jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    protected Map<String, JsonNode> createObject() {
        Map<String, JsonNode> map = newHashMap(node.size());
        for (Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> iterator = node.fields(); iterator.hasNext(); ) {
            Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry = iterator.next();
            map.put(entry.getKey(), new JacksonNode(entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        return map;
    }

    @Override
    protected SimpleType computeNodeType(com.fasterxml.jackson.databind.JsonNode node) {
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
            SimpleModule module = new SimpleModule();
            module.addDeserializer(JsonNode.class, new JacksonDeserializer());
            this.mapper = mapper.registerModule(module);
        }

        @Override
        public JsonNode wrap(Object node) {
            if (node instanceof JacksonNode) {
                JacksonNode providerNode = (JacksonNode) node;
                return providerNode.jsonPointer.isEmpty() ? providerNode : new JacksonNode((providerNode).node);
            } else if (node instanceof GenericNode) {
                GenericNode genericNode = (GenericNode) node;
                if (genericNode.getJsonPointer().isEmpty()) {
                    return genericNode;
                } else {
                    return genericNode.copy("");
                }
            } else if (node instanceof com.fasterxml.jackson.databind.JsonNode) {
                return new JacksonNode((com.fasterxml.jackson.databind.JsonNode) node);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of com.fasterxml.jackson.databind.JsonNode");
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            try {
                return mapper.readValue(rawJson, JsonNode.class);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}

final class JacksonDeserializer extends JsonDeserializer<JsonNode> {
    @Override
    public JsonNode deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return readNode(p, "");
    }

    @Override
    public JsonNode getNullValue(DeserializationContext ctx) {
        return new GenericNode("", SimpleType.NULL, null);
    }

    private JsonNode readNode(JsonParser p, String jsonPointer) throws IOException {
        switch (p.currentToken()) {
            case VALUE_NULL:
                return new GenericNode(jsonPointer, SimpleType.NULL, null);
            case VALUE_TRUE:
            case VALUE_FALSE:
                    return new GenericNode(jsonPointer, SimpleType.BOOLEAN, p.getBooleanValue());
            case VALUE_STRING:
                return new GenericNode(jsonPointer, SimpleType.STRING, p.getText());
            case VALUE_NUMBER_INT:
                return new GenericNode(jsonPointer, SimpleType.INTEGER, p.getBigIntegerValue());
            case VALUE_NUMBER_FLOAT:
                return readNumber(p, jsonPointer);
            case START_ARRAY:
                return readArray(p, jsonPointer);
            case START_OBJECT:
                return readObject(p, jsonPointer);
            case NOT_AVAILABLE:
            case END_OBJECT:
            case END_ARRAY:
            case FIELD_NAME:
            case VALUE_EMBEDDED_OBJECT:
            default:
                throw new UnsupportedOperationException(p.currentToken().name()); // todo better msg
        }
    }

    private JsonNode readNumber(JsonParser p, String jsonPointer) throws IOException {
        BigDecimal val = p.getDecimalValue();
        // todo reuse
        if (val.scale() <= 0 || val.stripTrailingZeros().scale() <= 0) {
            return new GenericNode(jsonPointer, SimpleType.INTEGER, val.toBigInteger());
        } else {
            return new GenericNode(jsonPointer, SimpleType.NUMBER, val);
        }
    }

    private JsonNode readArray(JsonParser p, String jsonPointer) throws IOException {
        List<JsonNode> arr = new ArrayList<>();
        while (p.nextToken() != JsonToken.END_ARRAY) {
            arr.add(readNode(p, jsonPointer + "/" + arr.size()));
        }
        return new GenericNode(jsonPointer, SimpleType.ARRAY, arr);
    }

    private JsonNode readObject(JsonParser p, String jsonPointer) throws IOException {
        Map<String, JsonNode> obj = new LinkedHashMap<>();
        while (p.nextToken() != JsonToken.END_OBJECT) {
            String name = p.currentName();
            p.nextToken();
            obj.put(name, readNode(p, jsonPointer + "/" + JsonNode.encodeJsonPointer(name)));
        }
        return new GenericNode(jsonPointer, SimpleType.OBJECT, obj);
    }
}
