import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.GsonNode;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class Java8Test {
    @Test
    void shouldInstantiateValidatorFactory() {
        new ValidatorFactory();
    }

    @Test
    void shouldPassForJacksonFactory() {
        new ValidatorFactory()
                .withJsonNodeFactory(new JacksonNode.Factory())
                .validate("{}", "{}");
    }

    @Test
    void shouldPassForDefaultFactory() {
        new ValidatorFactory().validate("{}", "{}");
    }

    @Test
    void shouldFailForGsonFactory() {
        assertThatThrownBy(
                () -> new ValidatorFactory()
                        .withJsonNodeFactory(new GsonNode.Factory())
                        .validate("{}", "{}"))
                .isInstanceOf(NoClassDefFoundError.class);
    }
}