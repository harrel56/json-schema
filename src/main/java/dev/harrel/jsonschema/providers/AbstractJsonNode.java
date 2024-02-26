package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract class AbstractJsonNode<T> implements JsonNode {
    final T node;
    final String jsonPointer;
    final SimpleType nodeType;

    Map<String, JsonNode> asObject;
    List<JsonNode> asArray;

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

    abstract SimpleType computeNodeType(T node);
}
