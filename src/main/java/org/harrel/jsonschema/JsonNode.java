package org.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface JsonNode {
    String getJsonPointer();

    boolean isNull();
    boolean isBoolean();
    boolean isString();
    boolean isInteger();
    boolean isNumber();
    boolean isArray();
    boolean isObject();

    boolean asBoolean();
    String asString();
    BigInteger asInteger();
    BigDecimal asNumber();
    List<JsonNode> asArray();
    Map<String, JsonNode> asObject();

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

    private static boolean compareArrays(List<JsonNode> arr1, List<JsonNode> arr2) {
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

    private static boolean compareObjects(Map<String, JsonNode> object1, Map<String, JsonNode> object2) {
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
