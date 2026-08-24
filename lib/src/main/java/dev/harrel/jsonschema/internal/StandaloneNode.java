package dev.harrel.jsonschema.internal;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static dev.harrel.jsonschema.internal.InternalProviderUtil.newHashMap;

/**
 * Internal base class for all JSON provider implementations.
 * Not part of the contract and not intended for external use.
 */
public final class StandaloneNode implements JsonNode {
    private final String jsonPointer;
    private final SimpleType type;
    private final Object value;
    private Object altNumber;

    public StandaloneNode(String jsonPointer, SimpleType type, Object value) {
        this(jsonPointer, type, value, null);
    }

    public StandaloneNode(String jsonPointer, SimpleType type, Object value, Object altNumber) {
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
        this.type = Objects.requireNonNull(type);
        this.value = value;
        this.altNumber = altNumber;
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
    @SuppressWarnings("unchecked")
    public List<JsonNode> asArray() {
        return (List<JsonNode>) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, JsonNode> asObject() {
        return (Map<String, JsonNode>) value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StandaloneNode)) {
            return false;
        }
        StandaloneNode that = (StandaloneNode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @SuppressWarnings("unchecked")
    public StandaloneNode copy(String jsonPointer) {
        if (isArray()) {
            List<StandaloneNode> li = (List<StandaloneNode>) value;
            List<StandaloneNode> copy = new ArrayList<>(li.size());
            for (int i = 0; i < li.size(); i++) {
                copy.add(li.get(i).copy(jsonPointer + "/" + i));
            }
            return new StandaloneNode(jsonPointer, type, copy);
        } else if (isObject()) {
            Map<String, StandaloneNode> map = (Map<String, StandaloneNode>) value;
            Map<String, StandaloneNode> copy = newHashMap(map.size());
            for (Map.Entry<String, StandaloneNode> entry : map.entrySet()) {
                copy.put(entry.getKey(), entry.getValue().copy(jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
            }
            return new StandaloneNode(jsonPointer, type, copy);
        } else {
            return new StandaloneNode(jsonPointer, type, value);
        }
    }
}
