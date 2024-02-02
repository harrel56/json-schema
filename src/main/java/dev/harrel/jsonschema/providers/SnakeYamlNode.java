package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class SnakeYamlNode implements JsonNode {
    private final Factory factory;
    private final Object node;
    private final String jsonPointer;
    private final SimpleType nodeType;

    private SnakeYamlNode(Factory factory, Object node, String jsonPointer) {
        this.factory = Objects.requireNonNull(factory);
        this.node = node;
        this.jsonPointer = Objects.requireNonNull(jsonPointer);
        this.nodeType = factory.computeNodeType(node);
    }

    public SnakeYamlNode(Factory factory, Object node) {
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
        List<JsonNode> elements = new ArrayList<>();
        for (Object o : (List<?>) node) {
            elements.add(new SnakeYamlNode(factory, o, jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, JsonNode> asObject() {
        Map<String, JsonNode> map = new HashMap<>();
        for (Map.Entry<String, ?> entry : ((Map<String, ?>) node).entrySet()) {
            map.put(entry.getKey(), new SnakeYamlNode(factory, entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        return map;
    }

    public static final class Factory extends SimpleJsonNodeFactory {
        @Override
        public JsonNode wrap(Object node) {
            if (isLiteral(node) || isArray(node) || isObject(node)) {
                return new SnakeYamlNode(this, node);
            } else if (node instanceof SnakeYamlNode) {
                return (SnakeYamlNode) node;
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of org.json.JSONObject, org.json.JSONArray or simple literal");
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            class CustomConstructor extends SafeConstructor {
                public CustomConstructor() {
                    super(new LoaderOptions());
                    AbstractConstruct constr = new AbstractConstruct() {
                        @Override
                        public Object construct(Node node) {
                            ScalarNode scalar = (ScalarNode) node;
                            return new BigDecimal(scalar.getValue());
                        }
                    };
                    this.yamlConstructors.put(Tag.FLOAT, constr);
                }
            }
            return new SnakeYamlNode(this, new Yaml(new CustomConstructor()).load(rawJson));
        }

        @Override
        boolean isNull(Object node) {
            return node == null;
        }

        @Override
        boolean isArray(Object node) {
            return node instanceof List;
        }

        @Override
        boolean isObject(Object node) {
            return node instanceof Map;
        }
    }
}
