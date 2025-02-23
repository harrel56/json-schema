package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.SimpleType;

import java.math.BigDecimal;
import java.math.BigInteger;

abstract class SimpleJsonNode extends AbstractJsonNode<Object> {
    SimpleJsonNode(Object node, String jsonPointer) {
        super(node, jsonPointer);
    }

    abstract boolean isNull(Object node);

    abstract boolean isArray(Object node);

    abstract boolean isObject(Object node);

    @Override
    SimpleType computeNodeType(Object node) {
        if (isNull(node)) {
            return SimpleType.NULL;
        } else if (isBoolean(node)) {
            rawNode = node;
            return SimpleType.BOOLEAN;
        } else if (isString(node)) {
            rawNode = node;
            return SimpleType.STRING;
        } else if (isDecimal(node)) {
            rawNode = asNumber(node).stripTrailingZeros();
            if (canConvertToInteger((BigDecimal) rawNode)) {
                return SimpleType.INTEGER;
            } else {
                return SimpleType.NUMBER;
            }
        } else if (isInteger(node)) {
            rawNode = asNumber(node);
            return SimpleType.INTEGER;
        } else if (isArray(node)) {
            return SimpleType.ARRAY;
        } else if (isObject(node)) {
            return SimpleType.OBJECT;
        }
        throw new IllegalArgumentException("Cannot assign type to node of class=" + node.getClass().getName());
    }

    private BigDecimal asNumber(Object node) {
        if (node instanceof BigDecimal) {
            return (BigDecimal) node;
        } else if (node instanceof BigInteger) {
            rawBigInt = (BigInteger) node;
            return new BigDecimal((BigInteger) node);
        } else if (node instanceof Double) {
            return BigDecimal.valueOf((Double) node);
        } else {
            return BigDecimal.valueOf(((Number) node).longValue());
        }
    }

    private boolean isBoolean(Object node) {
        return node instanceof Boolean;
    }

    private boolean isString(Object node) {
        return node instanceof Character || node instanceof String || node instanceof Enum;
    }

    private boolean isInteger(Object node) {
        return node instanceof Integer || node instanceof Long || node instanceof BigInteger;
    }

    private boolean isDecimal(Object node) {
        return node instanceof Double || node instanceof BigDecimal;
    }
}
