package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JettisonTest {
    @Test
    void shouldWrapForValidArgument() throws JSONException {
        JSONObject object = new JSONObject("{}");
        JsonNode wrap = new JettisonNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldWrapForJsonNode() throws JSONException {
        JettisonNode.Factory factory = new JettisonNode.Factory();
        var jsonNode = factory.wrap(new JSONObject("{}"));
        JsonNode wrap = factory.wrap(jsonNode);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() throws JsonProcessingException {
        com.fasterxml.jackson.databind.JsonNode object = new ObjectMapper().readTree("{}");
        JettisonNode.Factory factory = new JettisonNode.Factory();
        assertThatThrownBy(() -> factory.wrap(object))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailCreateForInvalidArgument() {
        JettisonNode.Factory factory = new JettisonNode.Factory();
        assertThatThrownBy(() -> factory.create("{"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    class Draft2020SpecificationTest extends dev.harrel.jsonschema.Draft2020SpecificationTest {
        @Override
        protected JsonNodeFactory getJsonNodeFactory() {
            return new JettisonNode.Factory();
        }
    }

    @Nested
    class Draft2019SpecificationTest extends dev.harrel.jsonschema.Draft2019SpecificationTest {
        @Override
        protected JsonNodeFactory getJsonNodeFactory() {
            return new JettisonNode.Factory();
        }
    }

    @Nested
    class JsonNodeTest extends dev.harrel.jsonschema.JsonNodeTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JettisonNode.Factory();
        }
    }

    @Nested
    class Draft2020EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft2020EvaluatorFactoryTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JettisonNode.Factory();
        }
    }

    @Nested
    class MetaSchemaTest extends dev.harrel.jsonschema.MetaSchemaTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JettisonNode.Factory();
        }
    }

    @Nested
    class VocabulariesTest extends dev.harrel.jsonschema.VocabulariesTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JettisonNode.Factory();
        }
    }
}
