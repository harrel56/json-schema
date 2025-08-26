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
    final AbstractJsonNode<T> parent;
    final Object segment;
    String _jsonPointer;
    Object _rawNode;
    BigInteger _rawBigInt;

    AbstractJsonNode(T node, AbstractJsonNode<T> parent, Object segment) {
        this.nodeType = computeNodeType(node);
        this.node = node;
        this.parent = parent;
        this.segment = Objects.requireNonNull(segment);
    }

    @Override
    public String getJsonPointer() {
        if (_jsonPointer != null) {
            return _jsonPointer;
        }
        StringBuilder sb = new StringBuilder();
        buildJsonPointer(sb);
        return sb.toString();
    }

    @Override
    public SimpleType getNodeType() {
        return nodeType;
    }

    @Override
    public boolean asBoolean() {
        return (Boolean) _rawNode;
    }

    @Override
    public String asString() {
        return Objects.toString(_rawNode);
    }

    @Override
    public BigInteger asInteger() {
        if (_rawBigInt == null) {
            _rawBigInt = asNumber().toBigInteger();
        }
        return _rawBigInt;
    }

    @Override
    public BigDecimal asNumber() {
        return (BigDecimal) _rawNode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final List<JsonNode> asArray() {
        if (this._rawNode == null) {
            _rawNode = unmodifiableList(createArray());
        }
        return (List<JsonNode>) _rawNode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Map<String, JsonNode> asObject() {
        if (this._rawNode == null) {
            _rawNode = unmodifiableMap(createObject());
        }
        return (Map<String, JsonNode>) _rawNode;
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
            return Objects.equals(_rawBigInt, other._rawBigInt);
        } else {
            return Objects.equals(_rawNode, other._rawNode);
        }
    }

    @Override
    public final int hashCode() {
        ensureInitialized();
        if (getNodeType() == SimpleType.INTEGER) {
            return Objects.hashCode(_rawBigInt);
        } else {
            return Objects.hashCode(_rawNode);
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

    private void buildJsonPointer(StringBuilder sb) {
        if (_jsonPointer != null) {
            sb.append(_jsonPointer);
            return;
        }
        if (parent != null) {
            parent.buildJsonPointer(sb);
            sb.append('/');
        }
        sb.append(segment instanceof String ? JsonNode.encodeJsonPointer((String) segment) : segment);
        _jsonPointer = sb.toString();
    }

    abstract List<JsonNode> createArray();
    abstract Map<String, JsonNode> createObject();
    abstract SimpleType computeNodeType(T node);

    static boolean canConvertToInteger(BigDecimal bigDecimal) {
        return bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0;
    }
}
