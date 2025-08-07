import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.harrel.jsonschema.*;
import dev.harrel.jsonschema.providers.GsonNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class GsonTest extends ProviderTestBundle {
    @Override
    public JsonNodeFactory getJsonNodeFactory() {
        return new GsonNode.Factory();
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
        JsonElement object = new JsonParser().parse("{}");
        JsonNode wrap = new GsonNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() {
        JsonNode node = new JsonNodeMock();
        JsonNodeFactory factory = getJsonNodeFactory();
        assertThatThrownBy(() -> factory.wrap(node))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
