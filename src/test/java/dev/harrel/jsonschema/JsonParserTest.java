package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonParserTest {
    private final JsonNodeFactory nodeFactory = new JacksonNode.Factory();
    private JsonParser jsonParser;

    @BeforeEach
    void setUp() {
        Dialect dialect = new Dialects.Draft2020Dialect();
        var evaluatorFactory = mock(EvaluatorFactory.class);
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        var metaSchemaValidator = mock(MetaSchemaValidator.class);
        when(metaSchemaValidator.processMetaSchema(any(), any(), any(), any()))
                .thenReturn(new MetaValidationData(new Dialects.Draft2020Dialect()));
        this.jsonParser = new JsonParser(Dialects.createOfficialDialectsMap(), dialect, evaluatorFactory, schemaRegistry, metaSchemaValidator);
    }

    @Test
    void disallowsNonEmptyFragmentsInId() {
        URI baseUri = URI.create("urn:test");
        JsonNode jsonNode = nodeFactory.create("""
                {
                  "$id": "https://test.com/path#/pointer"
                }""");

        assertThatThrownBy(() -> jsonParser.parseRootSchema(baseUri, jsonNode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [https://test.com/path#/pointer] cannot contain non-empty fragments");
    }

    @Test
    void disallowMaliciousReRegistrationToAliasedUri() {
        URI baseUri = URI.create("urn:test");
        JsonNode schema1 = nodeFactory.create("""
                {
                  "$id": "urn:id",
                  "type": "object"
                }""");
        jsonParser.parseRootSchema(baseUri, schema1);

        JsonNode schema2 = nodeFactory.create("""
                {
                  "type": "string"
                }""");
        assertThatThrownBy(() -> jsonParser.parseRootSchema(baseUri, schema2))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}