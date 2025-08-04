import dev.harrel.jsonschema.*;
import dev.harrel.jsonschema.providers.KotlinxJsonNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonElement;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KotlinxJsonTest extends ProviderTestBundle {
    @Override
    public JsonNodeFactory getJsonNodeFactory() {
        return new KotlinxJsonNode.Factory();
    }

    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForProperFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new KotlinxJsonNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldFailForDefaultFactory() {
        AssertionsForClassTypes.assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }

    @Test
    void shouldWrapForValidArgument() {
        JsonElement object = Json.Default.parseToJsonElement("{}");
        JsonNode wrap = getJsonNodeFactory().wrap(object);
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
