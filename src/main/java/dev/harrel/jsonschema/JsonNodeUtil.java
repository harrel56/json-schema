package dev.harrel.jsonschema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class JsonNodeUtil {
    private JsonNodeUtil() {}

    static Optional<Map<String, JsonNode>> getAsObject(JsonNode node) {
        return node.isObject() ? Optional.of(node.asObject()) : Optional.empty();
    }

    static Optional<Boolean> getBooleanField(Map<String, JsonNode> objectMap, String fieldName) {
        return Optional.ofNullable(objectMap.get(fieldName))
                .filter(JsonNode::isBoolean)
                .map(JsonNode::asBoolean);
    }

    static Optional<String> getStringField(Map<String, JsonNode> objectMap, String fieldName) {
        return Optional.ofNullable(objectMap.get(fieldName))
                .filter(JsonNode::isString)
                .map(JsonNode::asString);
    }

    static Optional<Map<String, JsonNode>> getObjectField(Map<String, JsonNode> objectMap, String fieldName) {
        return Optional.ofNullable(objectMap.get(fieldName))
                .filter(JsonNode::isObject)
                .map(JsonNode::asObject);
    }

    static Optional<Map<String, Boolean>> getVocabulariesObject(Map<String, JsonNode> objectNode) {
        return getObjectField(objectNode, Keyword.VOCABULARY)
                .map(obj -> obj.entrySet().stream()
                        .filter(entry -> entry.getValue().isBoolean())
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().asBoolean())))
                .map(Collections::unmodifiableMap);
    }

    static boolean equals(JsonNode node1, JsonNode node2) {
        if (node1.getNodeType() != node2.getNodeType()) {
            return false;
        }
        switch (node1.getNodeType()) {
            case NULL:
                return true;
            case BOOLEAN:
                return node1.asBoolean() == node2.asBoolean();
            case STRING:
                return node1.asString().equals(node2.asString());
            case INTEGER:
                return node1.asInteger().equals(node2.asInteger());
            case NUMBER:
                return node1.asNumber().equals(node2.asNumber());
            case ARRAY:
                return compareArrays(node1.asArray(), node2.asArray());
            case OBJECT:
                return compareObjects(node1.asObject(), node2.asObject());
            default:
                throw new IllegalArgumentException(String.format("Unknown nodeType [%s]", node1.getNodeType()));
        }
    }

    private static boolean compareArrays(List<JsonNode> arr1, List<JsonNode> arr2) {
        if (arr1.size() != arr2.size()) {
            return false;
        }
        for (int i = 0; i < arr1.size(); i++) {
            if (!equals(arr1.get(i), arr2.get(i))) {
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
            if (otherField == null || !equals(entry.getValue(), otherField)) {
                return false;
            }
        }
        return true;
    }
}
