import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.OrgJsonNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import org.assertj.core.api.AssertionsForClassTypes;
import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class OrgJsonTest {
    private JsonNodeFactory createFactory() {
        return new OrgJsonNode.Factory();
    }

    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForOrgJsonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new OrgJsonNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldFailForDefaultFactory() {
        assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }

    @Test
    void shouldWrapForValidArgument() {
        JSONObject object = new JSONObject("{}");
        JsonNode wrap = new OrgJsonNode.Factory().wrap(object);
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
