import dev.harrel.jsonschema.*;
import dev.harrel.jsonschema.providers.JakartaJsonNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import jakarta.json.Json;
import jakarta.json.JsonStructure;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class JakartaJsonTest extends ProviderTestBundle {
    @Override
    public JsonNodeFactory getJsonNodeFactory() {
        return new JakartaJsonNode.Factory();
    }

    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForGsonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new JakartaJsonNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldFailForDefaultFactory() {
        AssertionsForClassTypes.assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }

    @Test
    void shouldWrapForValidArgument() {
        JsonStructure object = Json.createReader(new StringReader("{}")).read();
        JsonNode wrap = new JakartaJsonNode.Factory().wrap(object);
        assertThat(wrap).isNotNull();
        assertThat(wrap.getNodeType()).isEqualTo(SimpleType.OBJECT);
    }

    @Test
    void shouldFailWrapForInvalidArgument() {
        JsonNode node = new JsonNodeMock();
        JsonNodeFactory factory = getJsonNodeFactory();
        AssertionsForClassTypes.assertThatThrownBy(() -> factory.wrap(node))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
