package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class VocabulariesTest {
    protected static JsonNodeFactory nodeFactory;

    @Test
    void name() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri(null)
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta",
                  "type": "null"
                }""";

        validator.registerSchema(metaSchema);
        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isTrue();
    }
}
