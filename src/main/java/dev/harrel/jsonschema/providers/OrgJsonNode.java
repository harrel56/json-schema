package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;

public final class OrgJsonNode extends SimpleJsonNode {

    private OrgJsonNode(Object node, OrgJsonNode parent, Object segment) {
        super(Objects.requireNonNull(node), parent, segment);
    }

    public OrgJsonNode(Object node) {
        this(node, null, "");
    }

    @Override
    List<JsonNode> createArray() {
        JSONArray jsonArray = (JSONArray) node;
        List<JsonNode> elements = new ArrayList<>(jsonArray.length());
        for (Object o : jsonArray) {
            elements.add(new OrgJsonNode(o, this, elements.size()));
        }
        return elements;
    }

    @Override
    Map<String, JsonNode> createObject() {
        JSONObject jsonObject = (JSONObject) node;
        Map<String, JsonNode> map = MapUtil.newHashMap(jsonObject.length());
        for (String key : jsonObject.keySet()) {
            map.put(key, new OrgJsonNode(jsonObject.get(key), this, key));
        }
        return map;
    }

    @Override
    boolean isNull(Object node) {
        return JSONObject.NULL.equals(node);
    }

    @Override
    boolean isArray(Object node) {
        return node instanceof JSONArray;
    }

    @Override
    boolean isObject(Object node) {
        return node instanceof JSONObject;
    }

    public static final class Factory implements JsonNodeFactory {
        @Override
        public JsonNode wrap(Object node) {
            if (node instanceof OrgJsonNode) {
                OrgJsonNode providerNode = (OrgJsonNode) node;
                return providerNode.parent == null ? providerNode : new OrgJsonNode((providerNode).node);
            } else {
                return new OrgJsonNode(node);
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            return new OrgJsonNode(new JSONTokener(rawJson).nextValue());
        }
    }
}
