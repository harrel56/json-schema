import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.harrel.jsonschema.*;
import dev.harrel.jsonschema.internal.StandaloneNode;
import dev.harrel.jsonschema.providers.GsonModule;
import dev.harrel.jsonschema.providers.GsonNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class GsonStandaloneNodeTest extends ProviderTestBundle {
    // factory to be promoted to prod code someday
    private final JsonNodeFactory standaloneFactory = new JsonNodeFactory() {
        private final Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(JsonNode.class, new GsonModule.TypeAdapter())
                .create();

        @Override
        public dev.harrel.jsonschema.JsonNode wrap(Object node) {
            return switch (node) {
                case StandaloneNode sNode when sNode.getJsonPointer().isEmpty() -> sNode;
                case StandaloneNode sNode -> sNode.copy("");
                case JsonNode otherNode -> gson.fromJson(gson.toJson(otherNode), JsonNode.class);
                case JsonElement providerNode -> gson.fromJson(gson.toJson(providerNode), JsonNode.class);
                default ->
                        throw new IllegalArgumentException("Cannot wrap an instance of " + node.getClass().getName());
            };
        }

        @Override
        public dev.harrel.jsonschema.JsonNode create(String rawJson) {
            return gson.fromJson(rawJson, JsonNode.class);
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
    void shouldPassForGsonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new GsonNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldFailForDefaultFactory() {
        assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }

    @Test
    void shouldWrapForValidArgument() {
        JsonElement object = new Gson().toJsonTree(Map.of());
        JsonNode wrap = new GsonNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldWrapRudimentaryJsonNodeImpl() {
        JsonNodeFactory factory = getJsonNodeFactory();
        JsonNode node = factory.wrap(new JsonNodeMock());
        assertThat(node.isBoolean()).isTrue();
        assertThat(node.asBoolean()).isFalse();
    }
}
