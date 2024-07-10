package dev.harrel.jsonschema;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.*;

class CompoundSchemaTest {
    private static final String FORMAT_META_SCHEMA = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:format",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true,
                    "https://json-schema.org/draft/2020-12/vocab/format-assertion": false
                  }
                }""";

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

    @Test
    void nestedSchemaTakesVocabsFromItsMetaSchema() {
        String compoundSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:root",
                  "format": "ipv4",
                  "$defs": {
                    "nested": {
                      "$schema": "urn:format",
                      "$id": "urn:nested",
                      "format": "ipv4"
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(FORMAT_META_SCHEMA);
        validator.registerSchema(compoundSchema);

        Validator.Result result = validator.validate(URI.create("urn:root"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested"), "\"hello\"");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void nestedSchemaInheritsVocabsFromParent() {
        String compoundSchema = """
                {
                  "$schema": "urn:format",
                  "$id": "urn:root",
                  "format": "ipv4",
                  "$defs": {
                    "nested": {
                      "$id": "urn:nested",
                      "format": "ipv4"
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(FORMAT_META_SCHEMA);
        validator.registerSchema(compoundSchema);

        Validator.Result result = validator.validate(URI.create("urn:root"), "\"hello\"");
        assertThat(result.isValid()).isFalse();

        result = validator.validate(URI.create("urn:nested"), "\"hello\"");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void deeplyNestedSchemaTakesVocabsFromItsMetaSchema() {
        String compoundSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:root",
                  "format": "ipv4",
                  "$defs": {
                    "nested": {
                      "$id": "urn:nested",
                      "format": "ipv4",
                      "$defs": {
                        "nested2": {
                          "$schema": "urn:format",
                          "$id": "urn:nested2",
                          "format": "ipv4"
                        }
                      }
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(FORMAT_META_SCHEMA);
        validator.registerSchema(compoundSchema);

        Validator.Result result = validator.validate(URI.create("urn:root"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested2"), "\"hello\"");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void deeplyNestedSchemaTakesVocabsFromNearestParent() {
        String compoundSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:root",
                  "format": "ipv4",
                  "$defs": {
                    "nested": {
                      "$schema": "urn:format",
                      "$id": "urn:nested",
                      "format": "ipv4",
                      "$defs": {
                        "nested2": {
                          "$id": "urn:nested2",
                          "format": "ipv4"
                        }
                      }
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(FORMAT_META_SCHEMA);
        validator.registerSchema(compoundSchema);

        Validator.Result result = validator.validate(URI.create("urn:root"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested"), "\"hello\"");
        assertThat(result.isValid()).isFalse();

        result = validator.validate(URI.create("urn:nested2"), "\"hello\"");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void deeplyNestedSchemaTakesVocabsFromNearestParentIgnoringFakeSubSchemas() {
        String compoundSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:root",
                  "format": "ipv4",
                  "$defs": {
                    "nested": {
                      "$schema": "urn:format",
                      "format": "ipv4",
                      "$defs": {
                        "nested2": {
                          "$id": "urn:nested2",
                          "format": "ipv4"
                        }
                      }
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(FORMAT_META_SCHEMA);
        validator.registerSchema(compoundSchema);

        Validator.Result result = validator.validate(URI.create("urn:root"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested2"), "\"hello\"");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void deeplyNestedSchemaInheritDefaultVocabs() {
        String compoundSchema = """
                {
                  "$id": "urn:root",
                  "format": "ipv4",
                  "$defs": {
                    "nested": {
                      "$id": "urn:nested",
                      "format": "ipv4",
                      "$defs": {
                        "nested2": {
                          "$id": "urn:nested2",
                          "format": "ipv4"
                        }
                      }
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(FORMAT_META_SCHEMA);
        validator.registerSchema(compoundSchema);

        Validator.Result result = validator.validate(URI.create("urn:root"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested2"), "\"hello\"");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void deeplyNestedRefChainUsesProperVocabs() {
        String compoundSchema = """
                {
                  "$id": "urn:root",
                  "$ref": "urn:nested",
                  "$defs": {
                    "another": {
                      "$id": "urn:another",
                      "format": "ipv4"
                    },
                    "nested": {
                      "$id": "urn:nested",
                      "$ref": "urn:nested2",
                      "$defs": {
                        "nested2": {
                          "$schema": "urn:format",
                          "$id": "urn:nested2",
                          "$ref": "urn:another"
                        }
                      }
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(FORMAT_META_SCHEMA);
        validator.registerSchema(compoundSchema);

        Validator.Result result = validator.validate(URI.create("urn:root"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested"), "\"hello\"");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:nested2"), "\"hello\"");
        assertThat(result.isValid()).isTrue();
    }
}
