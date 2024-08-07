package dev.harrel.jsonschema;

import java.util.Collections;
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
}
