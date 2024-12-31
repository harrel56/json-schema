import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.JettisonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IsolatedJettisonTest {
    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForJettisonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new JettisonNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldFailForDefaultFactory() {
        assertThatThrownBy(() -> new ValidatorFactory().validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }
}