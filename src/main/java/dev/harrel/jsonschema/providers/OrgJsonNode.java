package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class OrgJsonNode implements JsonNode {
    private final Factory factory;
    private final Object node;
    private final String jsonPointer;
    private final SimpleType nodeType;

    private OrgJsonNode(Factory factory, Object node, String jsonPointer) {
        this.factory = Objects.requireNonNull(factory);
        this.node = Objects.requireNonNull(node);
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
        this.nodeType = factory.computeNodeType(node);
    }

    public OrgJsonNode(Factory factory, Object node) {
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
        return node.toString();
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
        for (Object o : (JSONArray) node) {
            elements.add(new OrgJsonNode(factory, o, jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        Map<String, JsonNode> map = new HashMap<>();
        JSONObject jsonObject = (JSONObject) node;
        for (String key : jsonObject.keySet()) {
            map.put(key, new OrgJsonNode(factory, jsonObject.get(key), jsonPointer + "/" + key));
        }
        return map;
    }

    public static final class Factory extends SimpleJsonNodeFactory {
        @Override
        public JsonNode wrap(Object node) {
            if (isLiteral(node) || isArray(node) || isObject(node)) {
                return new OrgJsonNode(this, node);
            } else if (node instanceof OrgJsonNode) {
                return (OrgJsonNode) node;
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of org.json.JSONObject, org.json.JSONArray or simple literal");
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            return new OrgJsonNode(this, new JSONTokener(rawJson).nextValue());
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
    }
}
