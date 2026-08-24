import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.ProviderTestBundle;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.internal.StandaloneNode;
import dev.harrel.jsonschema.providers.GsonNode;
import dev.harrel.jsonschema.providers.JacksonModule;
import dev.harrel.jsonschema.providers.JacksonNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonStandaloneNodeTest extends ProviderTestBundle {
    // factory to be promoted to prod code someday
    private final JsonNodeFactory standaloneFactory = new JsonNodeFactory() {
        private final ObjectMapper mapper = new ObjectMapper().registerModule(new JacksonModule());

        @Override
        public dev.harrel.jsonschema.JsonNode wrap(Object node) {
            return switch (node) {
                case StandaloneNode sNode when sNode.getJsonPointer().isEmpty() -> sNode;
                case StandaloneNode sNode -> sNode.copy("");
                case dev.harrel.jsonschema.JsonNode otherNode -> mapper.convertValue(otherNode, dev.harrel.jsonschema.JsonNode.class);
                case JsonNode providerNode -> mapper.convertValue(providerNode, dev.harrel.jsonschema.JsonNode.class);
                default -> throw new IllegalArgumentException("Cannot wrap an instance of " + node.getClass().getName());
            };
        }

        @Override
        public dev.harrel.jsonschema.JsonNode create(String rawJson) {
            try {
                return mapper.readValue(rawJson, dev.harrel.jsonschema.JsonNode.class);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    };

    @Override
    public JsonNodeFactory getJsonNodeFactory() {
        return standaloneFactory;
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
    void shouldWrapForValidArgument() throws JsonProcessingException {
        JsonNode object = new ObjectMapper().readTree("{}");
        dev.harrel.jsonschema.JsonNode wrap = new JacksonNode.Factory().wrap(object);
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
