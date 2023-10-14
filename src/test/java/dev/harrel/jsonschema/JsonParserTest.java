package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class JsonParserTest {
    @Test
    void disallowsNonEmptyFragmentsInId() {
        Dialect dialect = new Dialects.Draft2020Dialect();
        var evaluatorFactory = mock(EvaluatorFactory.class);
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        var metaSchemaValidator = mock(MetaSchemaValidator.class);
        JsonParser jsonParser = new JsonParser(dialect, evaluatorFactory, schemaRegistry, metaSchemaValidator);
        URI baseUri = URI.create("urn:test");
        JsonNode jsonNode = new JacksonNode.Factory().create("""
                {
                  "$id": "https://test.com/path#/pointer"
                }""");

        assertThatThrownBy(() -> jsonParser.parseRootSchema(baseUri, jsonNode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [https://test.com/path#/pointer] cannot contain non-empty fragments");
    }
}