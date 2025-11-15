package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

import java.util.*;

public final class JettisonNode extends SimpleJsonNode {

    private JettisonNode(Object node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    public JettisonNode(Object node) {
        this(node, "");
    }

    @Override
    List<JsonNode> createArray() {
        JSONArray arrayNode = (JSONArray) node;
        List<JsonNode> elements = new ArrayList<>(arrayNode.length());
        for (int i = 0; i < arrayNode.length(); ++i) {
            elements.add(new JettisonNode(arrayNode.opt(i), jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    @SuppressWarnings("unchecked")
    Map<String, JsonNode> createObject() {
        JSONObject jsonObject = (JSONObject) node;
        Map<String, JsonNode> map = MapUtil.newHashMap(jsonObject.length());
        for (Object object : jsonObject.toMap().entrySet()) {
            Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) object;
            map.put(entry.getKey().toString(), new JettisonNode(entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey().toString())));
        }
        return map;
    }

    @Override
    boolean isNull(Object node) {
        return JSONObject.NULL.equals(node) || JSONObject.EXPLICIT_NULL.equals(node);
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
            if (node instanceof JettisonNode) {
                JettisonNode providerNode = (JettisonNode) node;
                return providerNode.jsonPointer.isEmpty() ? providerNode : new JettisonNode((providerNode).node);
            } else {
                return new JettisonNode(node);
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            try {
                return new JettisonNode(new BigDecimalTokener(rawJson).nextValue());
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static final class BigDecimalTokener extends JSONTokener {
        private BigDecimalTokener(String s) {
            super(s);
            this.useBigDecimal = true;
        }
    }
}
