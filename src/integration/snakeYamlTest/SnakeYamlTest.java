import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.SnakeYamlNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnakeYamlTest {
    private JsonNodeFactory createFactory() {
        return new SnakeYamlNode.Factory();
    }

    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForOrgJsonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new SnakeYamlNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldFailForDefaultFactory() {
        AssertionsForClassTypes.assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }

    @Test
    void shouldWrapForValidArgument() {
        Node object = new Yaml().compose(new StringReader("hello:"));
        JsonNode wrap = createFactory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() {
        JsonNode node = new JsonNodeMock();
        JsonNodeFactory factory = createFactory();
        AssertionsForClassTypes.assertThatThrownBy(() -> factory.wrap(node))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldTreatAllKeysAsStrings() {
        String yamlString = """
                1: 1
                1.2: 1.2
                true: true
                null: null
                nested1:
                  nested2:
                    0: 0
                """;
        JsonNode node = createFactory().create(yamlString);
        assertThat(node.isObject()).isTrue();
        Map<String, JsonNode> nodeMap = node.asObject();
        assertThat(nodeMap).containsOnlyKeys("1", "1.2", "true", "null", "nested1");
        assertThat(nodeMap.get("1").isInteger()).isTrue();
        assertThat(nodeMap.get("1").asInteger()).isEqualTo(1);
        assertThat(nodeMap.get("1.2").isNumber()).isTrue();
        assertThat(nodeMap.get("1.2").asNumber()).isEqualTo(BigDecimal.valueOf(1.2));
        assertThat(nodeMap.get("true").isBoolean()).isTrue();
        assertThat(nodeMap.get("true").asBoolean()).isTrue();
        assertThat(nodeMap.get("null").isNull()).isTrue();

        JsonNode nestedNode = nodeMap.get("nested1").asObject().get("nested2").asObject().get("0");
        assertThat(nestedNode.isInteger()).isTrue();
        assertThat(nestedNode.asInteger()).isZero();
    }

    @Test
    void shouldNotAllowDuplicateKeys() {
        String yamlString = """
                1: 1
                '1': '1'
                """;
        JsonNodeFactory factory = createFactory();
        assertThatThrownBy(() -> factory.create(yamlString))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("""
                        Mapping key '1' is duplicated in 'reader', line 2, column 1:
                            '1': '1'
                            ^""");
    }

    @Test
    void shouldNotAllowNestedDuplicateKeys() {
        String yamlString = """
                1: 1
                2:
                  - 1: 1
                  - 2:
                      1: 1
                      2:
                        3: 3
                        3: 3
                """;
        JsonNodeFactory factory = createFactory();
        assertThatThrownBy(() -> factory.create(yamlString))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("""
                        Mapping key '3' is duplicated in 'reader', line 8, column 9:
                                    3: 3
                                    ^""");
    }

    @Test
    void shouldHandleAliases() {
        String yamlString = """
                foo: &foo
                  bar:
                    - 1
                    - 2
                ref: *foo
                """;
        JsonNode node = createFactory().create(yamlString);
        assertThat(node.isObject()).isTrue();
        Map<String, JsonNode> nodeMap = node.asObject();
        assertThat(nodeMap).containsOnlyKeys("foo", "ref");
        Map<String, JsonNode> fooMap = nodeMap.get("foo").asObject();
        assertThat(fooMap.get("bar").isArray()).isTrue();
        List<JsonNode> barArray = fooMap.get("bar").asArray();
        assertThat(barArray.get(0).isInteger()).isTrue();
        assertThat(barArray.get(0).asInteger()).isEqualTo(BigInteger.valueOf(1));
        assertThat(barArray.get(1).isInteger()).isTrue();
        assertThat(barArray.get(1).asInteger()).isEqualTo(BigInteger.valueOf(2));

        Map<String, JsonNode> refMap = nodeMap.get("ref").asObject();
        assertThat(refMap.get("bar").isArray()).isTrue();
        List<JsonNode> barArray2 = refMap.get("bar").asArray();
        assertThat(barArray2.get(0).isInteger()).isTrue();
        assertThat(barArray2.get(0).asInteger()).isEqualTo(BigInteger.valueOf(1));
        assertThat(barArray2.get(1).isInteger()).isTrue();
        assertThat(barArray2.get(1).asInteger()).isEqualTo(BigInteger.valueOf(2));
    }

    @Test
    void shouldHandleRecursiveAliases() {
        String yamlString = """
                foo: &foo
                  bar:
                    - 1
                    - 2
                  nested: *foo
                """;
        JsonNode node = createFactory().create(yamlString);
        assertThat(node.isObject()).isTrue();
        Map<String, JsonNode> nodeMap = node.asObject();
        assertThat(nodeMap).containsOnlyKeys("foo");
        Map<String, JsonNode> fooMap = nodeMap.get("foo").asObject();
        assertThat(fooMap.get("bar").isArray()).isTrue();
        List<JsonNode> barArray = fooMap.get("bar").asArray();
        assertThat(barArray.get(0).isInteger()).isTrue();
        assertThat(barArray.get(0).asInteger()).isEqualTo(BigInteger.valueOf(1));
        assertThat(barArray.get(1).isInteger()).isTrue();
        assertThat(barArray.get(1).asInteger()).isEqualTo(BigInteger.valueOf(2));

        Map<String, JsonNode> nestedMap = fooMap.get("nested").asObject();
        assertThat(nestedMap.get("bar").isArray()).isTrue();
        List<JsonNode> barArray2 = nestedMap.get("bar").asArray();
        assertThat(barArray2.get(0).isInteger()).isTrue();
        assertThat(barArray2.get(0).asInteger()).isEqualTo(BigInteger.valueOf(1));
        assertThat(barArray2.get(1).isInteger()).isTrue();
        assertThat(barArray2.get(1).asInteger()).isEqualTo(BigInteger.valueOf(2));

        Map<String, JsonNode> nestedMap2 = nestedMap.get("nested").asObject();
        assertThat(nestedMap2.get("bar").isArray()).isTrue();
        List<JsonNode> barArray3 = nestedMap2.get("bar").asArray();
        assertThat(barArray3.get(0).isInteger()).isTrue();
        assertThat(barArray3.get(0).asInteger()).isEqualTo(BigInteger.valueOf(1));
        assertThat(barArray3.get(1).isInteger()).isTrue();
        assertThat(barArray3.get(1).asInteger()).isEqualTo(BigInteger.valueOf(2));
    }

    @Test
    @Disabled("When operating on Node instances it apparently does not support merge (override)")
    void shouldHandleAliasOverride() {
        String yamlString = """
                foo: &foo
                  name: foo
                  bar: bar
                ref:
                  <<: *foo
                  name: ref
                  new: true
                """;
        JsonNode node = createFactory().create(yamlString);
        assertThat(node.isObject()).isTrue();
        Map<String, JsonNode> nodeMap = node.asObject();
        assertThat(nodeMap).containsOnlyKeys("foo", "ref");
        Map<String, JsonNode> fooMap = nodeMap.get("foo").asObject();
        assertThat(fooMap.get("name").isString()).isTrue();
        assertThat(fooMap.get("name").asString()).isEqualTo("foo");
        assertThat(fooMap.get("bar").isString()).isTrue();
        assertThat(fooMap.get("bar").asString()).isEqualTo("bar");

        Map<String, JsonNode> refMap = nodeMap.get("ref").asObject();
        assertThat(refMap.get("name").isString()).isTrue();
        assertThat(refMap.get("name").asString()).isEqualTo("ref");
        assertThat(refMap.get("bar").isString()).isTrue();
        assertThat(refMap.get("bar").asString()).isEqualTo("bar");
        assertThat(refMap.get("new").isBoolean()).isTrue();
        assertThat(refMap.get("new").asBoolean()).isTrue();
    }

    @Nested
    class Yaml1_1ComplianceTest {
        @ParameterizedTest
        @ValueSource(strings = {"true", "True", "TRUE", "on", "On", "ON", "yes", "Yes", "YES"})
        void shouldSupportBooleanTruthyValues(String value) {
            JsonNode node = createFactory().create(value);
            assertThat(node.isBoolean()).isTrue();
            assertThat(node.asBoolean()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"false", "False", "FALSE", "off", "Off", "OFF", "no", "No", "NO"})
        void shouldSupportBooleanFalsyValues(String value) {
            JsonNode node = createFactory().create(value);
            assertThat(node.isBoolean()).isTrue();
            assertThat(node.asBoolean()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"0b111111", "077", "0x3f", "1:3"})
        void shouldSupportIntsInDifferentBase(String value) {
            JsonNode node = createFactory().create(value);
            assertThat(node.isInteger()).isTrue();
            assertThat(node.isNumber()).isTrue();
            assertThat(node.asInteger()).isEqualTo(63);
            assertThat(node.asNumber()).isEqualTo(BigDecimal.valueOf(63));
        }

        @Test
        void shouldSupportIntInBase60WithFloatingPoint() {
            JsonNode node = createFactory().create("12:21.0");
            assertThat(node.isInteger()).isTrue();
            assertThat(node.isNumber()).isTrue();
            assertThat(node.asInteger()).isEqualTo(741);
            assertThat(node.asNumber()).isEqualTo(BigDecimal.valueOf(741.0));
        }

        @Test
        void shouldSupportFloatsInBase60() {
            JsonNode node = createFactory().create("12:21.12");
            assertThat(node.isInteger()).isFalse();
            assertThat(node.isNumber()).isTrue();
            /* I believe it should be 741.2? Well, not sure how floating points should work in different bases */
            assertThat(node.asNumber()).isEqualTo(BigDecimal.valueOf(741.12));
        }

        /* This one I guess is kind of against the spec */
        @ParameterizedTest
        @ValueSource(strings = {".inf", "-.inf", ".Inf", ".INF", ".nan", "-.nan", ".Nan", ".NAN"})
        void shouldTreatSpecialFloatsAsStrings(String value) {
            JsonNode node = createFactory().create(value);
            assertThat(node.isString()).isTrue();
            assertThat(node.asString()).isEqualTo(value);
        }
    }

    @Nested
    class SpecificationSuiteTest extends dev.harrel.jsonschema.SpecificationSuiteTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class YamlSpecificationSuiteTest extends dev.harrel.jsonschema.YamlSpecificationSuiteTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class SupplementarySuiteTest extends dev.harrel.jsonschema.SupplementarySuiteTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class Draft2020EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft2020EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class Draft2019EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft2019EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class Draft7EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft7EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class Draft6EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft6EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class JsonNodeFactoryTest extends dev.harrel.jsonschema.JsonNodeFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class JsonNodeTest extends dev.harrel.jsonschema.JsonNodeTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class MetaSchemaTest extends dev.harrel.jsonschema.MetaSchemaTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class VocabulariesTest extends dev.harrel.jsonschema.VocabulariesTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class JsonPointerEscapingTest extends dev.harrel.jsonschema.JsonPointerEscapingTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class EvaluationPathTest extends dev.harrel.jsonschema.EvaluationPathTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class DisabledSchemaValidationTest extends dev.harrel.jsonschema.DisabledSchemaValidationTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }
}
