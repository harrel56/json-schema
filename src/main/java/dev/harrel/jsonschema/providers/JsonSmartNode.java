package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.util.*;

import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;

public final class JsonSmartNode extends SimpleJsonNode {

    private JsonSmartNode(Object node, String jsonPointer) {
        super(node, jsonPointer);
    }

    public JsonSmartNode(Object node) {
        this(node, "");
    }

    @Override
    public boolean asBoolean() {
        return (Boolean) node;
    }

    @Override
    public String asString() {
        return String.valueOf(node);
    }

    @Override
    public List<JsonNode> asArray() {
        JSONArray jsonArray = (JSONArray) node;
        List<JsonNode> result = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            result.add(new JsonSmartNode(jsonArray.get(i), jsonPointer + "/" + i));
        }
        return result;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Set<Map.Entry<String, Object>> objectMap = ((JSONObject) node).entrySet();
        Map<String, JsonNode> result = new HashMap<>(objectMap.size());
        for (Map.Entry<String, Object> entry : objectMap) {
            result.put(entry.getKey(), new JsonSmartNode(entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        return result;
    }

    @Override
    boolean isNull(Object node) {
        return node == null;
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
        private final JSONParser parser;

        public Factory() {
            this(new JSONParser(MODE_JSON_SIMPLE));
        }

        public Factory(JSONParser parser) {
            this.parser = parser;
        }

        @Override
        public JsonSmartNode wrap(Object node) {
            if (node instanceof JsonSmartNode) {
                return (JsonSmartNode) node;
            } else {
                return new JsonSmartNode(node);
            }
        }

        @Override
        public JsonSmartNode create(String rawJson) {
            try {
                return new JsonSmartNode(parser.parse(rawJson));
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
