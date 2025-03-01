package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.*;

abstract class AbstractJsonNode<T> implements JsonNode {
    private final SimpleType nodeType;
    final T node;
    final String jsonPointer;
    Object rawNode;
    BigInteger rawBigInt;

    AbstractJsonNode(T node, String jsonPointer) {
        this.nodeType = computeNodeType(node);
        this.node = node;
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public SimpleType getNodeType() {
        return nodeType;
    }

    @Override
    public boolean asBoolean() {
        return (Boolean) rawNode;
    }

    @Override
    public String asString() {
        return Objects.toString(rawNode);
    }

    @Override
    public BigInteger asInteger() {
        if (rawBigInt == null) {
            rawBigInt = asNumber().toBigInteger();
        }
        return rawBigInt;
    }

    @Override
    public BigDecimal asNumber() {
        return (BigDecimal) rawNode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final List<JsonNode> asArray() {
        if (this.rawNode == null) {
            rawNode = unmodifiableList(createArray());
        }
        return (List<JsonNode>) rawNode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Map<String, JsonNode> asObject() {
        if (this.rawNode == null) {
            rawNode = unmodifiableMap(createObject());
        }
        return (Map<String, JsonNode>) rawNode;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractJsonNode)) {
            return false;
        }
        AbstractJsonNode<?> other = (AbstractJsonNode<?>) obj;
        if (getNodeType() != other.getNodeType()) {
            return false;
        }
        ensureInitialized();
        other.ensureInitialized();
        if (getNodeType() == SimpleType.INTEGER) {
            return Objects.equals(rawBigInt, other.rawBigInt);
        } else {
            return Objects.equals(rawNode, other.rawNode);
        }
    }

    @Override
    public final int hashCode() {
        ensureInitialized();
        if (getNodeType() == SimpleType.INTEGER) {
            return Objects.hashCode(rawBigInt);
        } else {
            return Objects.hashCode(rawNode);
        }
    }

    private void ensureInitialized() {
        if (nodeType == SimpleType.INTEGER) {
            asInteger();
        } else if (nodeType == SimpleType.ARRAY) {
            asArray();
        } else if (nodeType == SimpleType.OBJECT) {
            asObject();
        }
    }

    abstract List<JsonNode> createArray();
    abstract Map<String, JsonNode> createObject();
    abstract SimpleType computeNodeType(T node);

    static boolean canConvertToInteger(BigDecimal bigDecimal) {
        return bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0;
    }
}
