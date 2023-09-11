package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonSmartTest {
    @Test
    void shouldWrapForValidArgument() throws ParseException {
        Object object = new JSONParser(MODE_JSON_SIMPLE).parse("{}");
        JsonNode wrap = new JsonSmartNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldWrapForJsonNode() throws ParseException {
        JsonSmartNode.Factory factory = new JsonSmartNode.Factory();
        var jsonNode = factory.wrap(new JSONParser(MODE_JSON_SIMPLE).parse("{}"));
        JsonNode wrap = factory.wrap(jsonNode);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() throws JsonProcessingException {
        com.fasterxml.jackson.databind.JsonNode object = new ObjectMapper().readTree("{}");
        JsonSmartNode.Factory factory = new JsonSmartNode.Factory();
        assertThatThrownBy(() -> factory.wrap(object))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    @Disabled
    class SpecificationTest extends dev.harrel.jsonschema.SpecificationTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JsonSmartNode.Factory();
        }
    }

    @Nested
    class JsonNodeTest extends dev.harrel.jsonschema.JsonNodeTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JsonSmartNode.Factory();
        }
    }

    @Nested
    class Draft2020EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft2020EvaluatorFactoryTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JsonSmartNode.Factory();
        }
    }

    @Nested
    class MetaSchemaTest extends dev.harrel.jsonschema.MetaSchemaTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JsonSmartNode.Factory();
        }
    }

    @Nested
    class VocabulariesTest extends dev.harrel.jsonschema.VocabulariesTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JsonSmartNode.Factory();
        }
    }
}
