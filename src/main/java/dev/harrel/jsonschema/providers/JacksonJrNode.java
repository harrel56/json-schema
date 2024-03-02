package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.jr.ob.JSON;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;

import java.io.IOException;
import java.util.*;

public final class JacksonJrNode extends SimpleJsonNode {

    private JacksonJrNode(Object node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    public JacksonJrNode(Object node) {
        this(node, "");
    }

    @Override
    public boolean asBoolean() {
        return (Boolean) node;
    }

    @Override
    public String asString() {
        return Objects.toString(isNull() ? null : node);
    }

    @Override
    List<JsonNode> createArray() {
        ArrayNode arrayNode = (ArrayNode) node;
        List<JsonNode> elements = new ArrayList<>();
        for (Iterator<com.fasterxml.jackson.databind.JsonNode> it = arrayNode.elements(); it.hasNext(); ) {
            com.fasterxml.jackson.databind.JsonNode jsonNode = it.next();
            elements.add(new JacksonJrNode(jsonNode, jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    Map<String, JsonNode> createObject() {
        TreeNode jsonObject = (TreeNode) node;
        Map<String, JsonNode> map = MapUtil.newHashMap(jsonObject.size());
        for (Iterator<String> it = jsonObject.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            map.put(fieldName, new JacksonJrNode(jsonObject.get(fieldName), jsonPointer + "/" + JsonNode.encodeJsonPointer(fieldName)));
        }
        return map;
    }

    @Override
    boolean isNull(Object node) {
        System.out.println("node class: " + node.getClass());
        return false;
    }

    @Override
    boolean isArray(Object node) {
        return node instanceof ArrayNode;
    }

    @Override
    boolean isObject(Object node) {
        return node instanceof TreeNode;
    }

    public static final class Factory implements JsonNodeFactory {
        @Override
        public JsonNode wrap(Object node) {
            if (node instanceof JacksonJrNode) {
                return (JacksonJrNode) node;
            } else {
                return new JacksonJrNode(node);
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            try {
                return new JacksonJrNode(JSON.std.anyFrom(rawJson));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
