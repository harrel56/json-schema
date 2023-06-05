package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONTokener;
import org.codehaus.jettison.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class JettisonNode implements JsonNode {
    private final Object node;
    private final String jsonPointer;
    private final SimpleType nodeType;

    private JettisonNode(Object node, String jsonPointer) {
        this.node = Objects.requireNonNull(node);
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
        this.nodeType = computeNodeType(node);
    }

    public JettisonNode(Object node) {
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
        if (Factory.isNull(node)) {
            return "null";
        } else {
            return node.toString();
        }
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
        List<JsonNode> elements = new ArrayList<>();
        JSONArray arrayNode = (JSONArray) node;
        for (int i = 0; i < arrayNode.length(); ++i) {
            elements.add(new JettisonNode(arrayNode.opt(i), jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, JsonNode> asObject() {
        Map<String, JsonNode> map = new HashMap<>();
        JSONObject jsonObject = (JSONObject) node;
        for (Object object : jsonObject.toMap().entrySet()) {
            Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) object;
            map.put(entry.getKey().toString(), new JettisonNode(entry.getValue(), jsonPointer + "/" + entry.getKey()));
        }
        return map;
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
        @Override
        public JsonNode wrap(Object node) {
            JSONArray tmp = new JSONArray();
            tmp.put(node);
            if (isLiteral(node) || isArray(node) || isObject(node)) {
                return new JettisonNode(node);
            } else if (node instanceof JsonNode) {
                return (JsonNode) node;
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of org.codehaus.jettison.json.JSONObject, org.codehaus.jettison.json.JSONArray or simple literal");
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            try {
                return new JettisonNode(new JSONTokener(rawJson).nextValue());
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private static boolean isLiteral(Object node) {
            return isNull(node) || isBoolean(node) || isString(node) || isInteger(node) || isDecimal(node);
        }

        private static boolean isNull(Object node) {
            return JSONObject.NULL.equals(node) || JSONObject.EXPLICIT_NULL.equals(node);
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
