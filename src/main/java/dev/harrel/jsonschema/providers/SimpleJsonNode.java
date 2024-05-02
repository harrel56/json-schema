package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;

abstract class SimpleJsonNode extends AbstractJsonNode<Object> {
    SimpleJsonNode(Object node, String jsonPointer) {
        super(node, jsonPointer);
    }

    @Override
    public BigInteger asInteger() {
        if (node instanceof BigInteger) {
            return (BigInteger) node;
        } else if (node instanceof BigDecimal) {
            return ((BigDecimal) node).toBigInteger();
        } else {
            return BigInteger.valueOf(((Number) node).longValue());
        }
    }

    @Override
    public BigDecimal asNumber() {
        if (node instanceof BigDecimal) {
            return (BigDecimal) node;
        } else if (node instanceof BigInteger) {
            return new BigDecimal((BigInteger) node);
        } else if (node instanceof Double) {
            return BigDecimal.valueOf((Double) node);
        } else {
            return BigDecimal.valueOf(((Number) node).longValue());
        }
    }

    boolean isBoolean(Object node) {
        return node instanceof Boolean;
    }

    boolean isString(Object node) {
        return node instanceof Character || node instanceof String || node instanceof Enum;
    }

    boolean isInteger(Object node) {
        return node instanceof Integer || node instanceof Long || node instanceof BigInteger;
    }

    boolean isDecimal(Object node) {
        return node instanceof Double || node instanceof BigDecimal;
    }

    abstract boolean isNull(Object node);

    abstract boolean isArray(Object node);

    abstract boolean isObject(Object node);

    @Override
    SimpleType computeNodeType(Object node) {
        if (isNull(node)) {
            return SimpleType.NULL;
        } else if (isBoolean(node)) {
            return SimpleType.BOOLEAN;
        } else if (isString(node)) {
            return SimpleType.STRING;
        } else if (isDecimal(node)) {
            if (node instanceof BigDecimal && ((BigDecimal) node).stripTrailingZeros().scale() <= 0) {
                return SimpleType.INTEGER;
            } else if (node instanceof Double && ((Number) node).doubleValue() == Math.rint(((Number) node).doubleValue())) {
                return SimpleType.INTEGER;
            } else {
                return SimpleType.NUMBER;
            }
        } else if (isInteger(node)) {
            return SimpleType.INTEGER;
        } else if (isArray(node)) {
            return SimpleType.ARRAY;
        } else if (isObject(node)) {
            return SimpleType.OBJECT;
        }
        throw new IllegalArgumentException("Cannot assign type to node of class=" + node.getClass().getName());
    }
}
