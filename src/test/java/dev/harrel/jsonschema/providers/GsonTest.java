package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GsonTest {
    @Test
    void shouldWrapForValidArgument() {
        JsonElement object = JsonParser.parseString("{}");
        JsonNode wrap = new GsonNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() throws JsonProcessingException {
        com.fasterxml.jackson.databind.JsonNode object = new ObjectMapper().readTree("{}");
        GsonNode.Factory factory = new GsonNode.Factory();
        assertThatThrownBy(() -> factory.wrap(object))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    class SpecificationTest extends dev.harrel.jsonschema.SpecificationTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }

    @Nested
    class JsonNodeTest extends dev.harrel.jsonschema.JsonNodeTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }

    @Nested
    class CoreValidatorFactoryTest extends dev.harrel.jsonschema.CoreValidatorFactoryTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }
    @Nested
    class MetaSchemaTest extends dev.harrel.jsonschema.MetaSchemaTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }

}
