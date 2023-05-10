import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.GsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IsolatedGsonTest {
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
}