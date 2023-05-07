package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(InvalidSchemaException.class);
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
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(InvalidSchemaException.class);
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
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(InvalidSchemaException.class);
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
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(InvalidSchemaException.class);
    }
}
