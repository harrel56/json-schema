package dev.harrel.jsonschema;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.*;

class CompoundSchemaTest {
    @Test
    @Disabled
    // fixme
    void compoundSchemaDoesntValidateEmbeddedSchemas() {
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$ref": "#/$defs/draft2019",
                  "$defs": {
                    "draft2019": {
                      "$schema": "https://json-schema.org/draft/2019-09/schema",
                      "$id": "urn:nested",
                      "items": [{
                        "type": "string"
                      }]
                    }
                  }
                }""";

        Validator.Result result = new ValidatorFactory()
                .validate(schema, "[1]");
        System.out.println(result);
    }

    @Test
    void compoundSchemaValidatesFakeEmbeddedSchemas() {
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$ref": "#/$defs/draft2019",
                  "$defs": {
                    "draft2019": {
                      "$schema": "https://json-schema.org/draft/2019-09/schema",
                      "items": [{
                        "type": "string"
                      }]
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        URI uri = URI.create("urn:test");
        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(uri, schema));
        assertThat(exception).hasMessage("Schema [urn:test] failed to validate against meta-schema [https://json-schema.org/draft/2020-12/schema]");
        assertThat(exception.getErrors()).hasSize(11); // TBH exact error count is not so important here
        assertThat(exception.getErrors().getFirst().getError()).isEqualTo("Value is [array] but should be [object, boolean]");
    }

    @Test
    void schemaKeywordIsIgnoredForFakeEmbeddedSchemas() {
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:compound",
                  "$ref": "#/$defs/x",
                  "$defs": {
                    "x": {
                      "$schema": "urn:this-resolves-to-nothing",
                      "type": ["null"]
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "{}");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertError(
                result.getErrors().getFirst(),
                "/$ref/type",
                "urn:compound#",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }

    @Test
    void fakeEmbeddedSchemasAreNotValidated() {
        String failingSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:failing",
                  "type": "string"
                }""";
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:compound",
                  "$ref": "#/$defs/x",
                  "$defs": {
                    "x": {
                      "$schema": "urn:failing",
                      "type": ["null"]
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        validator.registerSchema(failingSchema);
        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "{}");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertError(
                result.getErrors().getFirst(),
                "/$ref/type",
                "urn:compound#",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }
}
