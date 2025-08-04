import dev.harrel.jsonschema.*;
import dev.harrel.jsonschema.providers.OrgJsonNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import org.assertj.core.api.AssertionsForClassTypes;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class OrgJsonTest extends ProviderTestBundle {
    @Override
    public JsonNodeFactory getJsonNodeFactory() {
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
        JsonNodeFactory factory = getJsonNodeFactory();
        AssertionsForClassTypes.assertThatThrownBy(() -> factory.wrap(node))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
