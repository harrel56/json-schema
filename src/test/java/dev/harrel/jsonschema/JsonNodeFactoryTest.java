package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class JsonNodeFactoryTest implements ProviderTest {
    @Test
    void shouldWrapJsonNodeAndResetJsonPointer() {
        JsonNodeFactory factory = getJsonNodeFactory();
        JsonNode rootNode = factory.create("""
                {
                  "a": [
                    {
                      "b": null
                    }
                  ]
                }""");
        JsonNode nestedNode = rootNode.asObject().get("a").asArray().getFirst();

        assertThat(nestedNode.getJsonPointer()).isEqualTo("/a/0");
        assertThat(nestedNode.asObject().get("b").getJsonPointer()).isEqualTo("/a/0/b");

        JsonNode wrappedNode = factory.wrap(nestedNode);
        assertThat(wrappedNode.getJsonPointer()).isEmpty();
        assertThat(wrappedNode.asObject().get("b").getJsonPointer()).isEqualTo("/b");
    }

    @Test
    void shouldFailWrapForInvalidArgument() {
        Object object = new Object();
        JsonNodeFactory factory = getJsonNodeFactory();
        assertThatThrownBy(() -> factory.wrap(object))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailCreateForInvalidArgument() {
        JsonNodeFactory factory = getJsonNodeFactory();
        assertThatThrownBy(() -> factory.create("{"))
                .isInstanceOf(RuntimeException.class);
    }
}
