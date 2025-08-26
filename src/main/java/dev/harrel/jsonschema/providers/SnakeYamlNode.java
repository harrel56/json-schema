package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class SnakeYamlNode extends AbstractJsonNode<Node> {
    private static final SafeConstructor CONSTR = new SafeConstructor(new LoaderOptions());
    private static final SafeConstructor.ConstructYamlBool BOOLEAN_CREATOR = CONSTR.new ConstructYamlBool();
    private static final SafeConstructor.ConstructYamlInt INT_CREATOR = CONSTR.new ConstructYamlInt();
    private static final SafeConstructor.ConstructYamlFloat NUMBER_CREATOR = CONSTR.new ConstructYamlFloat();

    private SnakeYamlNode(Node node, SnakeYamlNode parent, Object segment) {
        super(Objects.requireNonNull(node), parent, segment);
    }

    private SnakeYamlNode(Node node) {
        this(node, null, "");
    }

    @Override
    List<JsonNode> createArray() {
        List<Node> arrayNode = ((SequenceNode) node).getValue();
        List<JsonNode> elements = new ArrayList<>(arrayNode.size());
        for (int i = 0; i < arrayNode.size(); i++) {
            elements.add(new SnakeYamlNode(arrayNode.get(i), this, i));
        }
        return elements;
    }

    @Override
    Map<String, JsonNode> createObject() {
        List<NodeTuple> objectNode = ((MappingNode) node).getValue();
        Map<String, JsonNode> map = MapUtil.newHashMap(objectNode.size());
        for (NodeTuple entry : objectNode) {
            String key = ((ScalarNode) entry.getKeyNode()).getValue();
            map.put(key, new SnakeYamlNode(entry.getValueNode(), this, key));
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
            _rawNode = BOOLEAN_CREATOR.construct(node);
            return SimpleType.BOOLEAN;
        } else if (node.getTag() == Tag.INT) {
            _rawNode = intToBigDecimal(node);
            return SimpleType.INTEGER;
        } else if (node.getTag() == Tag.FLOAT) {
            String asString = ((ScalarNode) node).getValue().toLowerCase();
            if (asString.contains(".inf") || asString.contains(".nan")) {
                _rawNode = ((ScalarNode) node).getValue();
                return SimpleType.STRING;
            }
            _rawNode = floatToBigDecimal(node);
            if (canConvertToInteger((BigDecimal) _rawNode)) {
                return SimpleType.INTEGER;
            } else {
                return SimpleType.NUMBER;
            }
        } else {
            _rawNode = ((ScalarNode) node).getValue();
            return SimpleType.STRING;
        }
    }

    private BigDecimal intToBigDecimal(Node node) {
        Object intObject = INT_CREATOR.construct(node);
        if (intObject instanceof Integer || intObject instanceof Long) {
            return BigDecimal.valueOf(((Number) intObject).longValue());
        } else {
            _rawBigInt = (BigInteger) intObject;
            return new BigDecimal((BigInteger) intObject);
        }
    }

    private static BigDecimal floatToBigDecimal(Node node) {
        String asString = ((ScalarNode) node).getValue();
        if (asString.contains(":")) {
            return BigDecimal.valueOf(((Double) NUMBER_CREATOR.construct(node)));
        } else {
            return new BigDecimal(asString);
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
                SnakeYamlNode providerNode = (SnakeYamlNode) node;
                return providerNode.parent == null ? providerNode : new SnakeYamlNode((providerNode).node);
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
