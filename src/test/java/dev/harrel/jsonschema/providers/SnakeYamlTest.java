package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SchemaResolver;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.util.YamlRemoteSchemaResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.parser.ParserException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnakeYamlTest {
    private JsonNodeFactory createFactory() {
        return new SnakeYamlNode.Factory();
    }

    @Test
    void shouldWrapForValidArgument() {
        Map<String, Object> object = new HashMap<>();
        JsonNode wrap = createFactory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldWrapForJsonNode() {
        JsonNodeFactory factory = createFactory();
        var jsonNode = factory.wrap(new HashMap<>());
        JsonNode wrap = factory.wrap(jsonNode);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() throws JsonProcessingException {
        com.fasterxml.jackson.databind.JsonNode object = new ObjectMapper().readTree("{}");
        JsonNodeFactory factory = createFactory();
        assertThatThrownBy(() -> factory.wrap(object))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailCreateForInvalidArgument() {
        JsonNodeFactory factory = createFactory();
        assertThatThrownBy(() -> factory.create("{"))
                .isInstanceOf(ParserException.class);
    }

    @Nested
    class SpecificationSuiteTest extends dev.harrel.jsonschema.SpecificationSuiteTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }
    }

    @Nested
    class YamlSpecificationSuiteTest extends dev.harrel.jsonschema.SpecificationSuiteTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return createFactory();
        }

        @Override
        public SchemaResolver createSchemaResolver() {
            return new YamlRemoteSchemaResolver();
        }

        @Override
        public String getTestPath() {
            return "/suite-yaml/tests";
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
