package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.*;

public abstract class MetaSchemaTest {

    protected static JsonNodeFactory nodeFactory;
    private static final String CUSTOM_META_SCHEMA = """
            {
                "type": "object",
                "maxProperties": 2
            }""";
    private static final String INVALID_META_SCHEMA = "{";


    private final SchemaResolver resolver = uri -> {
        if ("custom".equals(uri)) {
            return SchemaResolver.Result.fromString(CUSTOM_META_SCHEMA);
        } else if ("invalid".equals(uri)) {
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
                .withJsonNodeFactory(nodeFactory)
                .validate(rawSchema, "null");
    }

    @Test
    void shouldThrowForInvalidSchemaWhenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "type": []
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawSchema), InvalidSchemaException.class);
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldPassForValidSchemaWhenCustomMetaSchema() {
        String rawSchema = """
                {
                    "type": ["null"]
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri("custom")
                .withSchemaResolver(resolver)
                .validate(rawSchema, "null");
    }

    @Test
    void shouldFailForInvalidSchemaWhenCustomMetaSchema() {
        String rawSchema = """
                {
                    "type": "string",
                    "maxLength": 1,
                    "minLength": 1
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri("custom")
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawSchema), InvalidSchemaException.class);
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldPassForValidSchemaWhenNullMetaSchema() {
        String rawSchema = """
                {
                    "type": 1
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri(null)
                .validate(rawSchema, "null");
    }

    @Test
    void shouldFailIfCannotResolveMetaSchema() {
        String rawSchema = """
                {
                    "type": 1
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri("custom")
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
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri("invalid")
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
                            "$schema": "custom"
                        }
                    }
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
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
                            "$schema": "custom",
                            "type": "string",
                            "maxLength": 1
                        }
                    }
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawSchema), InvalidSchemaException.class);
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldPassForOverriddenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "$schema": "custom",
                    "type": 1
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .validate(rawSchema, "{}");
    }

    @Test
    void shouldFailForOverriddenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "$schema": "custom",
                    "maxLength": 1,
                    "minLength": 1
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawSchema), InvalidSchemaException.class);
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldFailForInvalidTopSchemaElement() {
        String rawSchema = "[]";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawSchema), InvalidSchemaException.class);
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
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .createValidator();

        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawSchema), InvalidSchemaException.class);
        assertThat(exception.getErrors()).isNotEmpty();
        assertThat(exception.getMessage()).contains("urn:my-schema");
    }

    @Test
    void shouldPassForValidRecursiveSchema() {
        String rawSchema = """
                {
                    "$schema": "urn:recursive-schema",
                    "$id": "urn:recursive-schema",
                    "type": "object"
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .validate(rawSchema, "{}");
    }

    @Test
    void shouldFailForInvalidRecursiveSchema() {
        String rawSchema = """
                {
                    "$schema": "urn:recursive-schema",
                    "$id": "urn:recursive-schema",
                    "type": "null"
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawSchema), InvalidSchemaException.class);
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldPassForValidRecursiveEmbeddedSchema() {
        String rawSchema = """
                {
                    "properties": {
                      "prop": {
                        "$schema": "urn:recursive-schema",
                        "$id": "urn:recursive-schema",
                        "type": "object"
                      }
                    }
                }""";
        new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .validate(rawSchema, "{}");
    }

    @Test
    void shouldFailForInvalidRecursiveEmbeddedSchema() {
        String rawSchema = """
                {
                    "properties": {
                      "prop": {
                        "$schema": "urn:recursive-schema",
                        "$id": "urn:recursive-schema",
                        "type": "null"
                      }
                    }
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .createValidator();
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawSchema), InvalidSchemaException.class);
        assertThat(exception.getErrors()).isNotEmpty();
    }

    @Test
    void shouldRestoreRegistryStateForInvalidRecursiveSchema() {
        String rawPassingSchema = """
                {
                  "$id": "urn:passing"
                }""";
        String rawFailingSchema = """
                {
                    "$schema": "urn:recursive-schema",
                    "$id": "urn:recursive-schema",
                    "type": "null"
                }""";
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .createValidator();

        validator.registerSchema(URI.create("urn:schema1"), rawPassingSchema);
        assertThat(validator.validate(URI.create("urn:schema1"), "{}").isValid()).isTrue();
        assertThat(validator.validate(URI.create("urn:passing"), "{}").isValid()).isTrue();

        InvalidSchemaException exception = catchThrowableOfType(() -> validator.registerSchema(rawFailingSchema), InvalidSchemaException.class);
        assertThat(exception.getErrors()).isNotEmpty();
        URI failingUri = URI.create("urn:recursive-schema");

        SchemaNotFoundException notFoundException = catchThrowableOfType(() -> validator.validate(failingUri, "null"), SchemaNotFoundException.class);
        assertThat(notFoundException).hasMessage("Couldn't find schema with uri [urn:recursive-schema]");
        assertThat(notFoundException.getRef()).isEqualTo("urn:recursive-schema");
        assertThat(validator.validate(URI.create("urn:schema1"), "{}").isValid()).isTrue();
        assertThat(validator.validate(URI.create("urn:passing"), "{}").isValid()).isTrue();
    }
}
