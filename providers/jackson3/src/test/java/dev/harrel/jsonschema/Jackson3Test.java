package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.GsonNode;
import dev.harrel.jsonschema.providers.JacksonNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Jackson3Test extends ProviderTestBundle {
    @Override
    public JsonNodeFactory getJsonNodeFactory() {
        return new JacksonNode.Factory();
    }

    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForJacksonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new JacksonNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldPassForDefaultFactory() {
        new ValidatorFactory().validate("{}", "{}");
    }

    @Test
    void shouldFailForGsonFactory() {
        AssertionsForClassTypes.assertThatThrownBy(
                        () -> new ValidatorFactory()
                                .withJsonNodeFactory(new GsonNode.Factory())
                                .validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }

    @Test
    void shouldWrapForValidArgument() {
        tools.jackson.databind.JsonNode object = new ObjectMapper().readTree("{}");
        JacksonNode wrap = new JacksonNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() {
        dev.harrel.jsonschema.JsonNode node = new JsonNodeMock();
        JacksonNode.Factory factory = new JacksonNode.Factory();
        assertThatThrownBy(() -> factory.wrap(node))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
