package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NoOpMetaSchemaValidatorTest {
    @Test
    void shouldAlwaysReturnPredefinedVocabularies() {
        Set<String> activeVocabs = Set.of("v1", "v2");
        var metaValidator = new MetaSchemaValidator.NoOpMetaSchemaValidator(activeVocabs);

        assertThat(metaValidator.validateSchema(mock(JsonParser.class), URI.create("x"), "y", mock(JsonNode.class)))
                .isEqualTo(activeVocabs);
        assertThat(metaValidator.determineActiveVocabularies(Map.of()))
                .isEqualTo(activeVocabs);
    }
}