package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

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
            return Optional.of(CUSTOM_META_SCHEMA);
        } else if ("invalid".equals(uri)) {
            return Optional.of(INVALID_META_SCHEMA);
        } else {
            return Optional.empty();
        }
    };

    @Test
    void shouldPassForValidSchemaWhenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "type": ["null"]
                }""";
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .build();
        URI uri = validator.registerSchema(rawSchema);
        validator.validate(uri, "null");
    }

    @Test
    void shouldThrowForInvalidSchemaWhenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "type": []
                }""";
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .build();
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(InvalidSchemaException.class);
    }

    @Test
    void shouldPassForValidSchemaWhenCustomMetaSchema() {
        String rawSchema = """
                {
                    "type": ["null"]
                }""";
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri("custom")
                .withSchemaResolver(resolver)
                .build();
        URI uri = validator.registerSchema(rawSchema);
        validator.validate(uri, "null");
    }

    @Test
    void shouldFailForInvalidSchemaWhenCustomMetaSchema() {
        String rawSchema = """
                {
                    "type": "string",
                    "maxLength": 1,
                    "minLength": 1
                }""";
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri("custom")
                .withSchemaResolver(resolver)
                .build();
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(InvalidSchemaException.class);
    }

    @Test
    void shouldPassForValidSchemaWhenNullMetaSchema() {
        String rawSchema = """
                {
                    "type": 1
                }""";
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri(null)
                .build();
        URI uri = validator.registerSchema(rawSchema);
        validator.validate(uri, "null");
    }

    @Test
    void shouldFailIfCannotResolveMetaSchema() {
        String rawSchema = """
                {
                    "type": 1
                }""";
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri("custom")
                .build();
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(MetaSchemaResolvingException.class);
    }

    @Test
    void shouldFailIfCannotParseMetaSchema() {
        String rawSchema = """
                {
                    "type": "string"
                }""";
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withDefaultMetaSchemaUri("invalid")
                .build();
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
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .build();
        URI uri = validator.registerSchema(rawSchema);
        validator.validate(uri, "{}");
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
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .build();
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
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .build();
        URI uri = validator.registerSchema(rawSchema);
        validator.validate(uri, "{}");
    }

    @Test
    void shouldFailForOverriddenDefaultMetaSchema() {
        String rawSchema = """
                {
                    "$schema": "custom",
                    "maxLength": 1,
                    "minLength": 1
                }""";
        Validator validator = Validator.builder()
                .withJsonNodeFactory(nodeFactory)
                .withSchemaResolver(resolver)
                .build();
        assertThatThrownBy(() -> validator.registerSchema(rawSchema))
                .isInstanceOf(InvalidSchemaException.class);
    }
}
