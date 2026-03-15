package dev.harrel.json.providers.jackson3;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.internal.AbstractJsonNode;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public class Deser extends ValueDeserializer<JsonNode> {
    @Override
    public JsonNode deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        return readNode(p, "");
    }

    @Override
    public JsonNode getNullValue(DeserializationContext ctx) {
        return new GenericNode("", SimpleType.NULL, null);
    }

    private JsonNode readNode(JsonParser p, String jsonPointer) {
        return switch (p.currentToken()) {
            case VALUE_NULL -> new GenericNode(jsonPointer, SimpleType.NULL, null);
            case VALUE_TRUE, VALUE_FALSE -> new GenericNode(jsonPointer, SimpleType.BOOLEAN, p.getBooleanValue());
            case VALUE_STRING -> new GenericNode(jsonPointer, SimpleType.STRING, p.getString());
            case VALUE_NUMBER_INT -> new GenericNode(jsonPointer, SimpleType.INTEGER, p.getBigIntegerValue());
            case VALUE_NUMBER_FLOAT -> readNumber(p,jsonPointer);
            case START_ARRAY -> readArray(p, jsonPointer);
            case START_OBJECT -> readObject(p, jsonPointer);
            case NOT_AVAILABLE,
                 END_OBJECT,
                 END_ARRAY,
                 PROPERTY_NAME,
                 VALUE_EMBEDDED_OBJECT -> throw new UnsupportedOperationException(p.currentToken().name());
        };
    }

    private JsonNode readNumber(JsonParser p, String jsonPointer) {
        BigDecimal val = p.getDecimalValue();
        if (val.scale() <= 0 || val.stripTrailingZeros().scale() <= 0) {
            return new GenericNode(jsonPointer, SimpleType.INTEGER, val.toBigInteger());
        } else {
            return new GenericNode(jsonPointer, SimpleType.NUMBER, val);
        }
    }

    private JsonNode readArray(JsonParser p, String jsonPointer) {
        List<JsonNode> arr = new ArrayList<>();
        while (p.nextToken() != JsonToken.END_ARRAY) {
            arr.add(readNode(p, jsonPointer + "/" + arr.size()));
        }
        return new GenericNode(jsonPointer, SimpleType.ARRAY, arr);
    }

    private JsonNode readObject(JsonParser p, String jsonPointer) {
        Map<String, JsonNode> obj = new LinkedHashMap<>();
        while (p.nextToken() != JsonToken.END_OBJECT) {
            String name = p.currentName();
            p.nextToken();
            obj.put(name, readNode(p, jsonPointer + "/" + JsonNode.encodeJsonPointer(name)));
        }
        return new GenericNode(jsonPointer, SimpleType.OBJECT, obj);
    }
}

class GenericNode implements JsonNode {
    final String jsonPointer;
    final SimpleType type;
    final Object value;
    Object altNumber;

    public GenericNode(String jsonPointer, SimpleType type, Object value) {
        this.jsonPointer = jsonPointer;
        this.type = type;
        this.value = value;
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public SimpleType getNodeType() {
        return type;
    }

    @Override
    public boolean asBoolean() {
        return (Boolean) value;
    }

    @Override
    public String asString() {
        return String.valueOf(value);
    }

    @Override
    public BigInteger asInteger() {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if (altNumber == null) {
            altNumber = ((BigDecimal) value).toBigInteger();
        }
        return (BigInteger) altNumber;
    }

    @Override
    public BigDecimal asNumber() {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (altNumber == null) {
            altNumber = new BigDecimal((BigInteger) value);
        }
        return (BigDecimal) altNumber;
    }

    @Override
    public List<JsonNode> asArray() {
        return (List<JsonNode>) value;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        return (Map<String, JsonNode>) value;
    }

    @Override
    public String toString() {
        return "[%s, %s] %s".formatted(type, jsonPointer.isBlank() ? "/" : jsonPointer, value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GenericNode that)) {
            return false;
        }
        return type == that.type && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    GenericNode copy(String jsonPointer) {
        if (isArray()) {
            List<GenericNode> li = (List<GenericNode>) value;
            List<GenericNode> copy = new ArrayList<>(li.size());
            for (int i = 0; i < li.size(); i++) {
                copy.add(li.get(i).copy(jsonPointer + "/" + i));
            }
            return new GenericNode(jsonPointer, type, copy);
        } else if (isObject()) {
            Map<String, GenericNode> map = (Map<String, GenericNode>) value;
            Map<String, GenericNode> copy = newHashMap(map.size());
            for (Map.Entry<String, GenericNode> entry : map.entrySet()) {
                copy.put(entry.getKey(), entry.getValue().copy(jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
            }
            return new GenericNode(jsonPointer, type, copy);
        } else {
            return new GenericNode(jsonPointer, type, value);
        }
    }

    private static <K, V> HashMap<K, V> newHashMap(int realCapacity) {
        return new HashMap<>((int) Math.ceil(realCapacity / 0.75));
    }
}
