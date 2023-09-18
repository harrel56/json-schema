package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class JettisonNode implements JsonNode {
    private final Factory factory;
    private final Object node;
    private final String jsonPointer;
    private final SimpleType nodeType;

    private JettisonNode(Factory factory, Object node, String jsonPointer) {
        this.factory = Objects.requireNonNull(factory);
        this.node = Objects.requireNonNull(node);
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
        this.nodeType = factory.computeNodeType(node);
    }

    public JettisonNode(Factory factory, Object node) {
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
        if (factory.isNull(node)) {
            return "null";
        } else {
            return node.toString();
        }
    }

    @Override
    public BigInteger asInteger() {
        if (node instanceof BigDecimal) {
            return ((BigDecimal) node).toBigInteger();
        } else {
            return BigInteger.valueOf(((Number) node).longValue());
        }
    }

    @Override
    public BigDecimal asNumber() {
        if (node instanceof BigDecimal) {
            return (BigDecimal) node;
        } else {
            return BigDecimal.valueOf(((Number) node).longValue());
        }
    }

    @Override
    public List<JsonNode> asArray() {
        List<JsonNode> elements = new ArrayList<>();
        JSONArray arrayNode = (JSONArray) node;
        for (int i = 0; i < arrayNode.length(); ++i) {
            elements.add(new JettisonNode(factory, arrayNode.opt(i), jsonPointer + "/" + elements.size()));
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
            map.put(entry.getKey().toString(), new JettisonNode(factory, entry.getValue(), jsonPointer + "/" + entry.getKey()));
        }
        return map;
    }

    public static final class Factory extends SimpleJsonNodeFactory {
        @Override
        public JsonNode wrap(Object node) {
            if (isLiteral(node) || isArray(node) || isObject(node)) {
                return new JettisonNode(this, node);
            } else if (node instanceof JettisonNode) {
                return (JettisonNode) node;
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of org.codehaus.jettison.json.JSONObject, org.codehaus.jettison.json.JSONArray or simple literal");
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            try {
                return new JettisonNode(this, new BigDecimalTokener(rawJson).nextValue());
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
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
    }

    static final class BigDecimalTokener extends JSONTokener {
        BigDecimalTokener(String s) {
            super(s);
            this.useBigDecimal = true;
        }
    }
}
