package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonTest {
    private JsonNodeFactory createFactory() {
        return new JacksonNode.Factory();
    }

    @Test
    void shouldWrapForValidArgument() throws JsonProcessingException {
        JsonNode object = new ObjectMapper().readTree("{}");
        JacksonNode wrap = new JacksonNode.Factory().wrap(object);
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
    class SpecificationSuiteTest extends dev.harrel.jsonschema.SpecificationSuiteTest {
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
