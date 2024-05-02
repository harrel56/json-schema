package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.*;

abstract class AbstractJsonNode<T> implements JsonNode {
    private final SimpleType nodeType;
    final T node;
    final String jsonPointer;

    private Map<String, JsonNode> asObject;
    private List<JsonNode> asArray;

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

    @Override
    public final List<JsonNode> asArray() {
        if (this.asArray != null) {
            return asArray;
        }
        this.asArray = unmodifiableList(createArray());
        return asArray;
    }

    @Override
    public final Map<String, JsonNode> asObject() {
        if (this.asObject != null) {
            return asObject;
        }
        this.asObject = unmodifiableMap(createObject());
        return asObject;
    }

    abstract List<JsonNode> createArray();
    abstract Map<String, JsonNode> createObject();
    abstract SimpleType computeNodeType(T node);
}
