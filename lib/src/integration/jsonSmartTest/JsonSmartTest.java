import dev.harrel.jsonschema.*;
import dev.harrel.jsonschema.providers.JsonSmartNode;
import dev.harrel.jsonschema.util.JsonNodeMock;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;
import static org.assertj.core.api.Assertions.assertThat;

class JsonSmartTest extends ProviderTestBundle {
    @Override
    public JsonNodeFactory getJsonNodeFactory() {
        return new JsonSmartNode.Factory();
    }

    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForOrgJsonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new JsonSmartNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldFailForDefaultFactory() {
        AssertionsForClassTypes.assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }

    @Test
    void shouldWrapForValidArgument() throws ParseException {
        Object object = new JSONParser(MODE_JSON_SIMPLE).parse("{}");
        JsonNode wrap = new JsonSmartNode.Factory().wrap(object);
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
