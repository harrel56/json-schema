import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.JsonSmartNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IsolatedJsonSmartTest {
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
        assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }
}