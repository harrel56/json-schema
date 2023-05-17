package dev.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static dev.harrel.jsonschema.ComparatorHelper.*;
import static dev.harrel.jsonschema.ComparatorHelper.compareObjects;

/**
 * {@code JsonNode} interface is the main abstraction for provider-agnostic JSON node.
 */
public interface JsonNode {
    /**
     * JSON pointer getter.
     * @return JSON pointer
     */
    String getJsonPointer();
    /**
     * Node type getter
     * @return type of a node
     */
    SimpleType getNodeType();

    /**
     * Checks if JSON node is null.
     */
    default boolean isNull() {
        return getNodeType() == SimpleType.NULL;
    }
    /**
     * Checks if JSON node is a boolean.
     */
    default boolean isBoolean() {
        return getNodeType() == SimpleType.BOOLEAN;
    }
    /**
     * Checks if JSON node is a string.
     */
    default boolean isString() {
        return getNodeType() == SimpleType.STRING;
    }
    /**
     * Checks if JSON node is an integer.
     */
    default boolean isInteger() {
        return getNodeType() == SimpleType.INTEGER;
    }
    /**
     * Checks if JSON node is a number.
     */
    default boolean isNumber() {
        return getNodeType() == SimpleType.NUMBER || getNodeType() == SimpleType.INTEGER;
    }
    /**
     * Checks if JSON node is an array.
     */
    default boolean isArray() {
        return getNodeType() == SimpleType.ARRAY;
    }
    /**
     * Checks if JSON node is an object.
     */
    default boolean isObject() {
        return getNodeType() == SimpleType.OBJECT;
    }

    /**
     * Returns JSON node as a boolean.
     * If JSON node is not of a boolean type, then the behaviour is undefined.
     */
    boolean asBoolean();
    /**
     * Returns JSON node as a string.
     * If JSON node is not of a string type, then the behaviour is undefined.
     */
    String asString();
    /**
     * Returns JSON node as an integer.
     * If JSON node is not of an integer type, then the behaviour is undefined.
     */
    BigInteger asInteger();
    /**
     * Returns JSON node as a number.
     * If JSON node is not of a number type, then the behaviour is undefined.
     */
    BigDecimal asNumber();
    /**
     * Returns JSON node as an array.
     * If JSON node is not of an array type, then the behaviour is undefined.
     */
    List<JsonNode> asArray();
    /**
     * Returns JSON node as an object.
     * If JSON node is not of an object type, then the behaviour is undefined.
     */
    Map<String, JsonNode> asObject();

    /**
     * Converts JSON node to printable string.
     * This method is only used for validation messages construction.
     */
    default String toPrintableString() {
        if (isObject()) {
            return "specific object";
        } else if (isArray()) {
            return "specific array";
        } else {
            return asString();
        }
    }

    /**
     * Equality check between two {@link JsonNode}s.
     * Must follow JSON equality rules, e.g. JSON properties order is not relevant.
     */
    default boolean isEqualTo(JsonNode other) {
        if (isNull() && other.isNull()) {
            return true;
        } else if (isBoolean() && other.isBoolean()) {
            return asBoolean() == other.asBoolean();
        } else if (isString() && other.isString()) {
            return asString().equals(other.asString());
        } else if (isInteger() && other.isInteger()) {
            return asInteger().equals(other.asInteger());
        } else if (isNumber() && other.isNumber()) {
            return asNumber().equals(other.asNumber());
        } else if (isArray() && other.isArray()) {
            return compareArrays(asArray(), other.asArray());
        } else if (isObject() && other.isObject()) {
            return compareObjects(asObject(), other.asObject());
        } else {
            return false;
        }
    }
}

class ComparatorHelper {
    static boolean compareArrays(List<JsonNode> arr1, List<JsonNode> arr2) {
        if (arr1.size() != arr2.size()) {
            return false;
        }
        for (int i = 0; i < arr1.size(); i++) {
            if (!arr1.get(i).isEqualTo(arr2.get(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean compareObjects(Map<String, JsonNode> object1, Map<String, JsonNode> object2) {
        if (object1.size() != object2.size()) {
            return false;
        }
        for (Map.Entry<String, JsonNode> entry : object1.entrySet()) {
            JsonNode otherField = object2.get(entry.getKey());
            if (otherField == null || !entry.getValue().isEqualTo(otherField)) {
                return false;
            }
        }
        return true;
    }
}
