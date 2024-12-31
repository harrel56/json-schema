import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.SnakeYamlNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IsolatedSnakeYamlTest {
    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForOrgJsonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new SnakeYamlNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldFailForDefaultFactory() {
        assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }
}