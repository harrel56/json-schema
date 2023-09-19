package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.json.stream.JsonParsingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JakartaJsonTest {
    @Test
    void shouldWrapForValidArgument() {
        JsonStructure object = Json.createReader(new StringReader("{}")).read();
        JsonNode wrap = new JakartaJsonNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldWrapForJsonNode() {
        JakartaJsonNode.Factory factory = new JakartaJsonNode.Factory();
        var jsonNode = factory.wrap(Json.createReader(new StringReader("{}")).read());
        JsonNode wrap = factory.wrap(jsonNode);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() throws JsonProcessingException {
        com.fasterxml.jackson.databind.JsonNode object = new ObjectMapper().readTree("{}");
        JakartaJsonNode.Factory factory = new JakartaJsonNode.Factory();
        assertThatThrownBy(() -> factory.wrap(object))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailCreateForInvalidArgument() {
        JakartaJsonNode.Factory factory = new JakartaJsonNode.Factory();
        assertThatThrownBy(() -> factory.create("{"))
                .isInstanceOf(JsonParsingException.class);
    }

    @Nested
    class SpecificationTest extends dev.harrel.jsonschema.SpecificationTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JakartaJsonNode.Factory();
        }
    }

    @Nested
    class JsonNodeTest extends dev.harrel.jsonschema.JsonNodeTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JakartaJsonNode.Factory();
        }
    }

    @Nested
    class Draft2020EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft2020EvaluatorFactoryTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JakartaJsonNode.Factory();
        }
    }

    @Nested
    class MetaSchemaTest extends dev.harrel.jsonschema.MetaSchemaTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JakartaJsonNode.Factory();
        }
    }

    @Nested
    class VocabulariesTest extends dev.harrel.jsonschema.VocabulariesTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JakartaJsonNode.Factory();
        }
    }
}
