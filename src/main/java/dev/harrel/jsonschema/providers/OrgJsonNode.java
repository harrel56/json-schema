package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class OrgJsonNode implements JsonNode {
    private final Object node;
    private final String jsonPointer;
    private final SimpleType nodeType;

    private OrgJsonNode(Object node, String jsonPointer) {
        this.node = node;
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
        this.nodeType = computeNodeType(node);
    }

    public OrgJsonNode(Object node) {
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
        String stringNode = (String) node;
        return stringNode.substring(1, stringNode.length() - 1);
    }

    @Override
    public BigInteger asInteger() {
        return null;
    }

    @Override
    public BigDecimal asNumber() {
        return null;
    }

    @Override
    public List<JsonNode> asArray() {
        List<JsonNode> elements = new ArrayList<>();
        for (Object o : (JSONArray) node) {
            elements.add(new OrgJsonNode(o, jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    public Map<String, JsonNode> asObject() {
        return null;
    }

    @Override
    public String toPrintableString() {
        if (isObject()) {
            return "specific object";
        } else if (isArray()) {
            return "specific array";
        } else if (isString()) {
            return asString();
        } else {
            return node.toString();
        }
    }

    private static SimpleType computeNodeType(Object node) {
        if (Factory.isNull(node)) {
            return SimpleType.NULL;
        } else if (Factory.isBoolean(node)) {
            return SimpleType.BOOLEAN;
        } else if (Factory.isString(node)) {
            return SimpleType.STRING;
        } else if (Factory.isInteger(node)) {
            return SimpleType.INTEGER;
        } else if (Factory.isDecimal(node)) {
            return SimpleType.NUMBER;
        } else if (Factory.isArray(node)) {
            return SimpleType.ARRAY;
        } else if (Factory.isObject(node)) {
            return SimpleType.OBJECT;
        }
        throw new IllegalArgumentException("Couldn't assign type to node of class=" + node.getClass().getName());
    }

    // TODO handle JSONString + don't allow POJOs?
    public static final class Factory implements JsonNodeFactory {
        @Override
        public JsonNode wrap(Object node) {
            if (isLiteral(node) || isArray(node) || isObject(node)) {
                return new OrgJsonNode(JSONObject.wrap(node));
            } else if (node instanceof JsonNode) {
                return (JsonNode) node;
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of org.json.JSONObject, org.json.JSONArray or simple literal");
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            JSONTokener tokener = new JSONTokener(rawJson);
            char firstChar = tokener.nextClean();
            if (firstChar == '{') {
                tokener.back();
                return new OrgJsonNode(new JSONObject(tokener));
            } else if (firstChar == '[') {
                tokener.back();
                return new OrgJsonNode(new JSONArray(tokener));
            } else {
                return new OrgJsonNode(JSONObject.stringToValue(rawJson));
            }
        }

        private static boolean isLiteral(Object node) {
            return isNull(node) || isBoolean(node) || isString(node) || isInteger(node) || isDecimal(node);
        }

        private static boolean isNull(Object node) {
            return JSONObject.NULL.equals(node);
        }

        private static boolean isBoolean(Object node) {
            return node instanceof Boolean;
        }

        private static boolean isString(Object node) {
            return node instanceof Character || node instanceof String || node instanceof Enum;
        }

        private static boolean isInteger(Object node) {
            return node instanceof Byte || node instanceof Short || node instanceof Integer || node instanceof Long || node instanceof BigInteger;
        }

        private static boolean isDecimal(Object node) {
            return node instanceof Float || node instanceof Double || node instanceof BigDecimal;
        }

        private static boolean isArray(Object node) {
            return node instanceof JSONArray;
        }

        private static boolean isObject(Object node) {
            return node instanceof JSONObject;
        }
    }
}
