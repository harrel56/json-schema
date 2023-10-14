package dev.harrel.jsonschema;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

final class JsonNodeUtil {
    private JsonNodeUtil() {}

    static Optional<Map<String, JsonNode>> getAsObject(JsonNode node) {
        return node.isObject() ? Optional.of(node.asObject()) : Optional.empty();
    }

    static Optional<String> getStringField(Map<String, JsonNode> objectMap, String fieldName) {
        return Optional.ofNullable(objectMap.get(fieldName))
                .filter(JsonNode::isString)
                .map(JsonNode::asString);
    }

    static void validateIdField(String id) {
        if (UriUtil.hasNonEmptyFragment(URI.create(id))) {
            throw new IllegalArgumentException(String.format("$id [%s] cannot contain non-empty fragments", id));
        }
    }
}
