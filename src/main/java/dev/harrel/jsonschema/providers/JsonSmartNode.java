package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;

public final class JsonSmartNode implements JsonNode {
    private final Factory factory;
    private final Object node;
    private final String jsonPointer;
    private final SimpleType nodeType;

    private JsonSmartNode(Factory factory, Object node, String jsonPointer) {
        this.factory = Objects.requireNonNull(factory);
        this.node = node;
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
        this.nodeType = factory.computeNodeType(this.node);
    }

    public JsonSmartNode(Factory factory, Object node) {
        this(factory, node, "");
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
    public boolean asBoolean() {
        return (Boolean) node;
    }

    @Override
    public String asString() {
        return String.valueOf(node);
    }

    @Override
    public BigInteger asInteger() {
        if (node instanceof BigInteger) {
            return (BigInteger) node;
        } else if (node instanceof BigDecimal) {
            return ((BigDecimal) node).toBigInteger();
        } else {
            return BigInteger.valueOf(((Number) node).longValue());
        }
    }

    @Override
    public BigDecimal asNumber() {
        if (node instanceof BigDecimal) {
            return (BigDecimal) node;
        } else if (node instanceof BigInteger) {
            return new BigDecimal((BigInteger) node);
        } else if (node instanceof Double) {
            return BigDecimal.valueOf((Double) node);
        } else {
            return BigDecimal.valueOf(((Number) node).longValue());
        }
    }

    @Override
    public List<JsonNode> asArray() {
        JSONArray jsonArray = (JSONArray) node;
        List<JsonNode> result = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            result.add(new JsonSmartNode(factory, jsonArray.get(i), jsonPointer + "/" + i));
        }
        return result;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Set<Map.Entry<String, Object>> objectMap = ((JSONObject) node).entrySet();
        Map<String, JsonNode> result = new HashMap<>(objectMap.size());
        for (Map.Entry<String, Object> entry : objectMap) {
            result.put(entry.getKey(), new JsonSmartNode(factory, entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        return result;
    }

    public static final class Factory extends SimpleJsonNodeFactory {
        private final JSONParser parser;

        public Factory() {
            this(new JSONParser(MODE_JSON_SIMPLE));
        }

        public Factory(JSONParser parser) {
            this.parser = parser;
        }

        @Override
        public JsonSmartNode wrap(Object node) {
            if (isLiteral(node) || isArray(node) || isObject(node)) {
                return new JsonSmartNode(this, node);
            } else if (node instanceof JsonSmartNode) {
                return (JsonSmartNode) node;
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of net.minidev.json.JSONObject, net.minidev.json.JSONArray or simple literal");
            }
        }

        @Override
        public JsonSmartNode create(String rawJson) {
            try {
                return new JsonSmartNode(this, parser.parse(rawJson));
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
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
    }
}
