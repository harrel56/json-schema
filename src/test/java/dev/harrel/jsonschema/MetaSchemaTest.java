package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.*;

public abstract class MetaSchemaTest implements ProviderTest {
    private static final String CUSTOM_META_SCHEMA = """
            {
                "type": "object",
                "maxProperties": 2
            }""";
    private static final String INVALID_META_SCHEMA = "{";

    static class CustomDialect extends Dialects.Draft2020Dialect {
        @Override
        public String getMetaSchema() {
            return "urn:custom";
        }
    }

    static class InvalidCustomDialect extends Dialects.Draft2020Dialect {
        @Override
        public String getMetaSchema() {
            return "urn:invalid";
        }
    }


    private final SchemaResolver resolver = uri -> {
        if ("urn:custom".equals(uri)) {
            return SchemaResolver.Result.fromString(CUSTOM_META_SCHEMA);
        } else if ("urn:invalid".equals(uri)) {
            return SchemaResolver.Result.fromString(INVALID_META_SCHEMA);
        } else {
            return SchemaResolver.Result.empty();
        }
    };

    @Test
    void shouldPassForValidSchemaWhenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "type": ["null"]
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .validate(rawSchema, "null");
    }

    @Test
    void shouldThrowForInvalidSchemaWhenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "type": []
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldPassForValidSchemaWhenCustomMetaSchema() {
        String rawSchema = """
                {
                    "$schema": "urn:custom",
                    "type": ["null"]
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .validate(rawSchema, "null");
    }

    @Test
    // This actually unnecessarily wraps "recursive" exception - is it desired behavior? tbd
    void shouldFailRecursiveCustomMetaSchemaFromDialect() {
        String rawSchema = "{}";
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new CustomDialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        MetaSchemaResolvingException exception = catchThrowableOfType(MetaSchemaResolvingException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception).hasMessage("Parsing meta-schema [urn:custom] failed");
        assertThat(exception).hasCauseInstanceOf(MetaSchemaResolvingException.class);
        assertThat(exception.getCause()).hasMessage("Parsing meta-schema [urn:custom] failed - only dialects explicitly added to a validator can be recursive");
    }

    @Test
    void shouldPassForValidSchemaWhenDisabledSchemaValidation() {
        String rawSchema = """
                {
                    "type": 1
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDisabledSchemaValidation(true)
                .validate(rawSchema, "null");
    }

    @Test
    void shouldFailIfCannotResolveMetaSchema() {
        String rawSchema = """
                {
                    "type": 1
                }""";
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new CustomDialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(MetaSchemaResolvingException.class);
    }

    @Test
    void shouldFailIfCannotParseMetaSchema() {
        String rawSchema = """
                {
                    "type": "string"
                }""";
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new InvalidCustomDialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(MetaSchemaResolvingException.class);
    }

    @Test
    void shouldPassForValidEmbeddedSchema() {
        String rawSchema = """
                {
                    "type": "object",
                    "properties": {
                        "embedded": {
                            "$schema": "urn:custom"
                        }
                    }
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .validate(rawSchema, "{}");
    }

    @Test
    void shouldFailForInvalidEmbeddedSchema() {
        String rawSchema = """
                {
                    "type": "object",
                    "properties": {
                        "embedded": {
                            "$schema": "urn:custom",
                            "$id": "urn:embedded",
                            "type": "string",
                            "maxLength": 1
                        }
                    }
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldPassForOverriddenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "$schema": "urn:custom",
                    "type": 1
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .validate(rawSchema, "{}");
    }

    @Test
    void shouldFailForOverriddenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "$schema": "urn:custom",
                    "maxLength": 1,
                    "minLength": 1
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldFailForInvalidTopSchemaElement() {
        String rawSchema = "[]";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldContainProvidedSchemaIdInException() {
        String rawSchema = """
                {
                    "$id": "urn:my-schema",
                    "maxLength": "not a number"
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();

        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception.getErrors()).isNotEmpty();
        assertThat(exception.getMessage()).contains("urn:my-schema");
    }

    @Test
    void shouldFailForInvalidRecursiveMetaSchema() {
        // only official meta-schemas can be recursive, thus the overriding
        String rawSchema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "$id": "https://json-schema.org/draft/2020-12/schema",
                    "type": "null"
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldSupportRecursiveRegisteredCustomMetaSchemas() {
        String rawSchema = """
                {
                    "$schema": "urn:meta",
                    "$id": "urn:meta",
                    "type": "null"
                }""";
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2020Dialect() {
                    @Override
                    public String getMetaSchema() {
                        return "urn:meta";
                    }
                })
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception).hasMessage("Schema [urn:meta] failed to validate against meta-schema [urn:meta]");
        assertThat(exception.getErrors()).hasSize(1);
        assertError(
                exception.getErrors().getFirst(),
                "/type",
                "urn:meta#",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }

    @Test
    void shouldNotSupportRecursiveUnregisteredCustomMetaSchemas() {
        String rawSchema = """
                {
                    "$schema": "urn:meta",
                    "$id": "urn:meta",
                    "type": "null"
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        MetaSchemaResolvingException exception = catchThrowableOfType(MetaSchemaResolvingException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception).hasMessage("Parsing meta-schema [urn:meta] failed - only dialects explicitly added to a validator can be recursive");
    }

    @Test
    void shouldSupportRecursiveRegisteredCustomMetaSchemasEmbeddedSchema() {
        String rawSchema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "$id": "urn:compound",
                    "$defs": {
                        "embedded": {
                            "$schema": "urn:meta",
                            "$id": "urn:meta",
                            "type": "null"
                        }
                    }
                }""";
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2020Dialect() {
                    @Override
                    public String getMetaSchema() {
                        return "urn:meta";
                    }
                })
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception).hasMessage("Schema [urn:meta] failed to validate against meta-schema [urn:meta]");
        assertThat(exception.getErrors()).hasSize(1);
        assertError(
                exception.getErrors().getFirst(),
                "/$defs/embedded/type",
                "urn:compound#/$defs/embedded",
                "/$defs/embedded",
                "type",
                "Value is [object] but should be [null]"
        );
    }

    @Test
    void shouldNotSupportRecursiveUnregisteredCustomMetaSchemasEmbeddedSchema() {
        String rawSchema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "$defs": {
                        "embedded": {
                            "$schema": "urn:meta",
                            "$id": "urn:meta",
                            "type": "null"
                        }
                    }
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();
        MetaSchemaResolvingException exception = catchThrowableOfType(MetaSchemaResolvingException.class, () -> validator.registerSchema(rawSchema));
        assertThat(exception).hasMessage("Parsing meta-schema [urn:meta] failed - only dialects explicitly added to a validator can be recursive");
    }

    @Test
    void shouldRestoreRegistryStateForInvalidRecursiveSchema() {
        String rawPassingSchema = """
                {
                  "$id": "urn:passing"
                }""";
        String rawFailingSchema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "$id": "https://json-schema.org/draft/2020-12/schema",
                    "type": "null"
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withSchemaResolver(resolver)
                .createValidator();

        validator.registerSchema(URI.create("urn:schema1"), rawPassingSchema);
        assertThat(validator.validate(URI.create("urn:schema1"), "{}").isValid()).isTrue();
        assertThat(validator.validate(URI.create("urn:passing"), "{}").isValid()).isTrue();

        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(rawFailingSchema));
        assertThat(exception.getErrors()).hasSize(1);
        assertError(
                exception.getErrors().getFirst(),
                "/type",
                "https://json-schema.org/draft/2020-12/schema#",
                "",
                "type",
                "Value is [object] but should be [null]"
        );

        // check that draft2020 meta-schema was not overwritten
        URI emptySchemaUri = validator.registerSchema("""
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema"
                }""");

        assertThat(validator.validate(emptySchemaUri, "{}").isValid()).isTrue();
        assertThat(validator.validate(URI.create("urn:schema1"), "{}").isValid()).isTrue();
        assertThat(validator.validate(URI.create("urn:passing"), "{}").isValid()).isTrue();
    }

    @Test
    void shouldRestoreRegistryStateForExceptionDuringParsing() {
        String rawPassingSchema = """
                {
                  "$id": "urn:passing"
                }""";
        String rawFailingSchema = """
                {
                  "$id": "urn:root-schema",
                  "$defs": {
                    "x": {
                      "$id": "urn:embedded-schema",
                      "fail": null
                    }
                  }
                }""";
        EvaluatorFactory evaluatorFactory = (ctx, name, node) -> {
            if (name.equals("fail")) {
                throw new IllegalArgumentException("failing");
            } else {
                return Optional.empty();
            }
        };
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withEvaluatorFactory(evaluatorFactory)
                .withDisabledSchemaValidation(true)
                .createValidator();

        validator.registerSchema(URI.create("urn:schema1"), rawPassingSchema);
        assertThat(validator.validate(URI.create("urn:schema1"), "{}").isValid()).isTrue();
        assertThat(validator.validate(URI.create("urn:passing"), "{}").isValid()).isTrue();

        assertThatThrownBy(() -> validator.registerSchema(rawFailingSchema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("failing");
        URI rootUri = URI.create("urn:root-schema");
        URI failingUri = URI.create("urn:embedded-schema");

        SchemaNotFoundException notFoundException1 = catchThrowableOfType(SchemaNotFoundException.class, () -> validator.validate(failingUri, "null"));
        assertThat(notFoundException1).hasMessage("Couldn't find schema with uri [urn:embedded-schema]");
        assertThat(notFoundException1.getRef()).isEqualTo("urn:embedded-schema");
        SchemaNotFoundException notFoundException2 = catchThrowableOfType(SchemaNotFoundException.class, () -> validator.validate(rootUri, "null"));
        assertThat(notFoundException2).hasMessage("Couldn't find schema with uri [urn:root-schema]");
        assertThat(notFoundException2.getRef()).isEqualTo("urn:root-schema");
        assertThat(validator.validate(URI.create("urn:schema1"), "{}").isValid()).isTrue();
        assertThat(validator.validate(URI.create("urn:passing"), "{}").isValid()).isTrue();
    }

    @Test
    void validatesAgainstCustomMetaSchema() {
        String failingMetaSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:meta",
                  "type": "null"
                }""";
        String schema = """
                {
                  "$schema": "urn:meta",
                  "$id": "urn:schema"
                }""";
        Validator validator = new ValidatorFactory().withSchemaResolver(uri -> {
            if ("urn:meta".equals(uri)) {
                return SchemaResolver.Result.fromString(failingMetaSchema);
            } else {
                return SchemaResolver.Result.empty();
            }
        }).createValidator();

        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator.registerSchema(schema));
        assertThat(exception).hasMessage("Schema [urn:schema] failed to validate against meta-schema [urn:meta]");
        assertThat(exception.getErrors()).hasSize(1);
        assertError(
                exception.getErrors().getFirst(),
                "/type",
                "urn:meta",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }
}
