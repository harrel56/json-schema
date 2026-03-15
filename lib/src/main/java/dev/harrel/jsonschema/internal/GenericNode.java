package dev.harrel.jsonschema.internal;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Internal base class for all JSON provider implementations.
 * Not part of the contract and not intend for external use.
 */
public final class GenericNode implements JsonNode {
    private final String jsonPointer;
    private final SimpleType type;
    private final Object value;
    private Object altNumber;

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
    public boolean equals(Object o) {
        if (!(o instanceof GenericNode)) {
            return false;
        }
        GenericNode that = (GenericNode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public GenericNode copy(String jsonPointer) {
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

    // todo reuse
    private static <K, V> HashMap<K, V> newHashMap(int realCapacity) {
        return new HashMap<>((int) Math.ceil(realCapacity / 0.75));
    }
}
