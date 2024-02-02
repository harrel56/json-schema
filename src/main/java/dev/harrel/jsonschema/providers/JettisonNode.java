package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class JettisonNode extends SimpleJsonNode {
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
        return Objects.toString(isNull() ? null : node);
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
                return (JettisonNode) node;
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

    static final class BigDecimalTokener extends JSONTokener {
        BigDecimalTokener(String s) {
            super(s);
            this.useBigDecimal = true;
        }
    }
}
