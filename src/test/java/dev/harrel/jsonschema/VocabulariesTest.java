package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class VocabulariesTest implements ProviderTest {
    @Test
    void shouldRunEvaluatorsOnlyFromActiveVocabularies() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
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

    @Test
    void shouldIgnoreOptionalUnsupportedVocabularies() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true,
                    "urn:unknown": false
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";

        validator.registerSchema(metaSchema);
        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldFailForUnsupportedRequiredVocabularies() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true,
                    "urn:unknown": true
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";

        validator.registerSchema(metaSchema);
        URI schemaUri = URI.create("urn:schema");
        assertThatThrownBy(() -> validator.registerSchema(schemaUri, schema))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Following vocabularies [urn:unknown] are required but not supported");
    }

    @Test
    void shouldFailForMissingRequiredVocabularies() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta",
                  "$vocabulary": {}
                }""";
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";

        validator.registerSchema(metaSchema);
        URI schemaUri = URI.create("urn:schema");
        assertThatThrownBy(() -> validator.registerSchema(schemaUri, schema))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Required vocabularies [https://json-schema.org/draft/2020-12/vocab/core] were missing or marked optional in $vocabulary object");
    }

    @Test
    void shouldUseAllSupportedVocabulariesByDefault() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta"
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
        assertThat(result.isValid()).isFalse();
    }
}
