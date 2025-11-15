package dev.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * {@code JsonNode} interface is the main abstraction for provider-agnostic JSON node.
 */
public interface JsonNode {
    /**
     * Encodes all illegal characters in JSON pointer segment
     * @param pointer JSON pointer segment
     * @return encoded JSON pointer segment
     */
    static String encodeJsonPointer(String pointer) {
        return pointer.replace("~", "~0").replace("/", "~1");
    }
    /**
     * JSON pointer getter.
     * @return JSON pointer
     */
    String getJsonPointer();
    /**
     * Node type getter
     * @return type of node
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
        } else if (isInteger()) {
          return asInteger().toString();
        } else {
            return asString();
        }
    }
}
