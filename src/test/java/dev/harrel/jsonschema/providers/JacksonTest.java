package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.SimpleType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonTest {
    @Test
    void shouldWrapForValidArgument() throws JsonProcessingException {
        JsonNode object = new ObjectMapper().readTree("{}");
        JacksonNode wrap = new JacksonNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldWrapForJsonNode() throws JsonProcessingException {
        JacksonNode.Factory factory = new JacksonNode.Factory();
        var jsonNode = factory.wrap(new ObjectMapper().readTree("{}"));
        JacksonNode wrap = factory.wrap(jsonNode);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() throws JsonProcessingException {
        Integer object = new ObjectMapper().readValue("1", Integer.class);
        JacksonNode.Factory factory = new JacksonNode.Factory();
        assertThatThrownBy(() -> factory.wrap(object))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    @Disabled
    class SpecificationTest extends dev.harrel.jsonschema.SpecificationTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }

    @Nested
    class JsonNodeTest extends dev.harrel.jsonschema.JsonNodeTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }

    @Nested
    class Draft2020EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft2020EvaluatorFactoryTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }

    @Nested
    class MetaSchemaTest extends dev.harrel.jsonschema.MetaSchemaTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }

    @Nested
    class VocabulariesTest extends dev.harrel.jsonschema.VocabulariesTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }
}
