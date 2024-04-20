package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class SnakeYamlNode extends AbstractJsonNode<Node> {
    private BigDecimal asNumber;

    private SnakeYamlNode(Node node, String jsonPointer) {
        super(Objects.requireNonNull(node), jsonPointer);
    }

    private SnakeYamlNode(Node node) {
        this(node, "");
    }

    @Override
    public boolean asBoolean() {
        return Boolean.parseBoolean(((ScalarNode) node).getValue());
    }

    @Override
    public String asString() {
        return ((ScalarNode) node).getValue();
    }

    @Override
    public BigInteger asInteger() {
        if (asNumber == null) {
            asNumber = new BigDecimal(((ScalarNode) node).getValue());
        }
        return asNumber.toBigInteger();
    }

    @Override
    public BigDecimal asNumber() {
        if (asNumber == null) {
            asNumber = new BigDecimal(((ScalarNode) node).getValue());
        }
        return asNumber;
    }

    @Override
    List<JsonNode> createArray() {
        List<Node> arrayNode = ((SequenceNode) node).getValue();
        List<JsonNode> elements = new ArrayList<>(arrayNode.size());
        for (int i = 0; i < arrayNode.size(); i++) {
            elements.add(new SnakeYamlNode(arrayNode.get(i), jsonPointer + "/" + i));
        }
        return elements;
    }

    @Override
    Map<String, JsonNode> createObject() {
        List<NodeTuple> objectNode = ((MappingNode) node).getValue();
        Map<String, JsonNode> map = MapUtil.newHashMap(objectNode.size());
        for (NodeTuple entry : objectNode) {
            String key = ((ScalarNode) entry.getKeyNode()).getValue();
            map.put(key, new SnakeYamlNode(entry.getValueNode(), jsonPointer + "/" + JsonNode.encodeJsonPointer(key)));
        }
        return map;
    }

    @Override
    SimpleType computeNodeType(Node node) {
        if (node instanceof SequenceNode) {
            return SimpleType.ARRAY;
        } else if (node instanceof MappingNode) {
            return SimpleType.OBJECT;
        }

        if (node.getTag() == Tag.NULL) {
            return SimpleType.NULL;
        } else if (node.getTag() == Tag.BOOL) {
            return SimpleType.BOOLEAN;
        } else if (node.getTag() == Tag.INT) {
            return SimpleType.INTEGER;
        } else if (node.getTag() == Tag.FLOAT) {
            String asString = ((ScalarNode) node).getValue();
            asNumber = new BigDecimal(asString);
            if (asNumber.scale() <= 0 || asNumber.stripTrailingZeros().scale() <= 0) {
                return SimpleType.INTEGER;
            } else {
                return SimpleType.NUMBER;
            }
        } else {
            return SimpleType.STRING;
        }
    }

    public static final class Factory implements JsonNodeFactory {
        private final Yaml yaml;

        public Factory() {
            this(new Yaml());
        }

        public Factory(Yaml yaml) {
            this.yaml = yaml;
        }

        @Override
        public JsonNode wrap(Object node) {
            if (node instanceof SnakeYamlNode) {
                return new SnakeYamlNode(((SnakeYamlNode) node).node);
            } else if (node instanceof Node) {
                Node providerNode = (Node) node;
                assertKeyUniqueness(providerNode);
                return new SnakeYamlNode(providerNode);
            } else {
                throw new IllegalArgumentException("Cannot wrap object which is not an instance of org.yaml.snakeyaml.nodes.Node");
            }
        }

        @Override
        public JsonNode create(String rawJson) {
            Node node = yaml.compose(new StringReader(rawJson));
            assertKeyUniqueness(node);
            return new SnakeYamlNode(node);
        }

        private static void assertKeyUniqueness(Node node) {
            assertKeyUniqueness(node, Collections.newSetFromMap(new IdentityHashMap<>()));
        }

        private static void assertKeyUniqueness(Node node, Set<Node> visited) {
            if (!(node instanceof CollectionNode<?>) || !visited.add(node)) {
                return;
            } else if (node instanceof SequenceNode) {
                for (Node element : ((SequenceNode) node).getValue()) {
                    assertKeyUniqueness(element, visited);
                }
            } else if (node instanceof MappingNode) {
                List<NodeTuple> tuples = ((MappingNode) node).getValue();
                Set<String> keys = new HashSet<>();
                for (NodeTuple tuple : tuples) {
                    ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                    if (!keys.add(keyNode.getValue())) {
                        throw new IllegalArgumentException("Mapping key '" + keyNode.getValue() + "' is duplicated" + keyNode.getStartMark());
                    }
                    assertKeyUniqueness(tuple.getValueNode(), visited);
                }
            }
            visited.remove(node);
        }
    }
}
