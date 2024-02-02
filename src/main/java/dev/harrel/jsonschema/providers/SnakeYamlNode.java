package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SnakeYamlNode extends SimpleJsonNode {

    private SnakeYamlNode(Object node, String jsonPointer) {
        super(node, jsonPointer);
    }

    public SnakeYamlNode(Object node) {
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
        List<JsonNode> elements = new ArrayList<>();
        for (Object o : (List<?>) node) {
            elements.add(new SnakeYamlNode(o, jsonPointer + "/" + elements.size()));
        }
        return elements;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, JsonNode> asObject() {
        Map<String, JsonNode> map = new HashMap<>();
        for (Map.Entry<String, ?> entry : ((Map<String, ?>) node).entrySet()) {
            map.put(entry.getKey(), new SnakeYamlNode(entry.getValue(), jsonPointer + "/" + JsonNode.encodeJsonPointer(entry.getKey())));
        }
        return map;
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

    public static final class Factory implements JsonNodeFactory {
        private final Yaml yaml;

        public Factory() {
            this(new Yaml(new BigDecimalConstructor()));
        }

        public Factory(Yaml yaml) {
            this.yaml = yaml;
        }

        @Override
        public JsonNode wrap(Object node) {
            if (node instanceof SnakeYamlNode) {
                return (SnakeYamlNode) node;
            } else {
                return new SnakeYamlNode(node);
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            return new SnakeYamlNode(this, yaml.load(rawJson));
        }
    }

    private static final class BigDecimalConstructor extends SafeConstructor {
        private BigDecimalConstructor() {
            super(new LoaderOptions());
            AbstractConstruct constr = new AbstractConstruct() {
                @Override
                public Object construct(Node node) {
                    return new BigDecimal(((ScalarNode) node).getValue());
                }
            };
            this.yamlConstructors.put(Tag.FLOAT, constr);
        }
    }
}
