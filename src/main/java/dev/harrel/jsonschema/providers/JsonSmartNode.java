package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
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
    private final Object node;
    private final String jsonPointer;
    private final SimpleType nodeType;

    private JsonSmartNode(Object node, String jsonPointer) {
        this.node = node;
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
        this.nodeType = computeNodeType(this.node);
    }

    public JsonSmartNode(Object node) {
        this(node, "");
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
            result.add(new JsonSmartNode(jsonArray.get(i), jsonPointer + "/" + i));
        }
        return result;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Set<Map.Entry<String, Object>> objectMap = ((JSONObject) node).entrySet();
        Map<String, JsonNode> result = new HashMap<>(objectMap.size());
        for (Map.Entry<String, Object> entry : objectMap) {
            result.put(entry.getKey(), new JsonSmartNode(entry.getValue(), jsonPointer + "/" + entry.getKey()));
        }
        return result;
    }

    private static SimpleType computeNodeType(Object node) {
        if (Factory.isNull(node)) {
            return SimpleType.NULL;
        } else if (Factory.isBoolean(node)) {
            return SimpleType.BOOLEAN;
        } else if (Factory.isString(node)) {
            return SimpleType.STRING;
        } else if (Factory.isDecimal(node)) {
            if (node instanceof BigDecimal && ((BigDecimal) node).stripTrailingZeros().scale() <= 0) {
                return SimpleType.INTEGER;
            } else if (node instanceof Double && ((Number) node).doubleValue() == Math.rint(((Number) node).doubleValue())) {
                return SimpleType.INTEGER;
            } else {
                return SimpleType.NUMBER;
            }
        } else if (Factory.isInteger(node)) {
            return SimpleType.INTEGER;
        } else if (Factory.isArray(node)) {
            return SimpleType.ARRAY;
        } else if (Factory.isObject(node)) {
            return SimpleType.OBJECT;
        }
        throw new IllegalArgumentException("Cannot assign type to node of class=" + node.getClass().getName());
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
            if (isLiteral(node) || isArray(node) || isObject(node)) {
                return new JsonSmartNode(node);
            } else if (node instanceof JsonSmartNode) {
                return (JsonSmartNode) node;
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of net.minidev.json.JSONObject, net.minidev.json.JSONArray or simple literal");
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

        private static boolean isLiteral(Object node) {
            return isNull(node) || isBoolean(node) || isString(node) || isInteger(node) || isDecimal(node);
        }

        private static boolean isNull(Object node) {
            return node == null;
        }

        private static boolean isBoolean(Object node) {
            return node instanceof Boolean;
        }

        private static boolean isString(Object node) {
            return node instanceof Character || node instanceof String || node instanceof Enum;
        }

        private static boolean isInteger(Object node) {
            return node instanceof Integer || node instanceof Long || node instanceof BigInteger;
        }

        private static boolean isDecimal(Object node) {
            return node instanceof Double || node instanceof BigDecimal;
        }

        private static boolean isArray(Object node) {
            return node instanceof JSONArray;
        }

        private static boolean isObject(Object node) {
            return node instanceof JSONObject;
        }
    }
}
