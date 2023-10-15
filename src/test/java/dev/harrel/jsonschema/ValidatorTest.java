package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.harrel.jsonschema.TestUtil.assertAnnotation;
import static dev.harrel.jsonschema.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorTest {
    private static final URI SCHEMA_URI = URI.create("urn:test");
    private static final String RAW_SCHEMA = "{\"not\":{\"type\":\"boolean\"}}";
    private static final String RAW_INSTANCE = "null";

    @Test
    void failsForNonExistentSchema() {
        Validator validator = new ValidatorFactory().createValidator();
        validator.registerSchema(SCHEMA_URI, RAW_SCHEMA);
        URI invalidUri = URI.create("urn:test2");
        assertThatThrownBy(() -> validator.validate(invalidUri, RAW_INSTANCE))
                .isInstanceOf(SchemaNotFoundException.class)
                .hasMessageContaining(invalidUri.toString());
    }

    @Test
    void failsForUriWithNonEmptyFragments() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = URI.create("https://test.com/x#/$def/x");
        assertThatThrownBy(() -> validator.validate(uri, "{}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Root schema [https://test.com/x#/$def/x] cannot contain non-empty fragments");
    }

    @Test
    void registersUriWithEmptyFragments() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = URI.create("https://test.com/x#");
        assertThatThrownBy(() -> validator.validate(uri, "{}"))
                .isInstanceOf(SchemaNotFoundException.class)
                .hasMessage("Couldn't find schema with uri [https://test.com/x#]");
    }

    @Test
    void registersSchemaString() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = validator.registerSchema(SCHEMA_URI, RAW_SCHEMA);
        boolean valid = validator.validate(uri, RAW_INSTANCE).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void registersSchemaProviderNode() throws JsonProcessingException {
        Validator validator = new ValidatorFactory().createValidator();
        Object providerNode = new ObjectMapper().readTree(RAW_SCHEMA);
        URI uri = validator.registerSchema(SCHEMA_URI, providerNode);
        boolean valid = validator.validate(uri, RAW_INSTANCE).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void registersSchemaJsonNode() {
        Validator validator = new ValidatorFactory().createValidator();
        JsonNode jsonNode = new JacksonNode.Factory().create(RAW_SCHEMA);
        URI uri = validator.registerSchema(SCHEMA_URI, jsonNode);
        boolean valid = validator.validate(uri, RAW_INSTANCE).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void registersAnonymousSchemaString() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = validator.registerSchema(RAW_SCHEMA);
        boolean valid = validator.validate(uri, RAW_INSTANCE).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void registersAnonymousSchemaProviderNode() throws JsonProcessingException {
        Validator validator = new ValidatorFactory().createValidator();
        Object providerNode = new ObjectMapper().readTree(RAW_SCHEMA);
        URI uri = validator.registerSchema(providerNode);
        boolean valid = validator.validate(uri, RAW_INSTANCE).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void registersAnonymousSchemaJsonNode() {
        Validator validator = new ValidatorFactory().createValidator();
        JsonNode jsonNode = new JacksonNode.Factory().create(RAW_SCHEMA);
        URI uri = validator.registerSchema(jsonNode);
        boolean valid = validator.validate(uri, RAW_INSTANCE).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void validatesInstanceString() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = validator.registerSchema(RAW_SCHEMA);
        boolean valid = validator.validate(uri, RAW_INSTANCE).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void validatesInstanceProviderNode() throws JsonProcessingException {
        Validator validator = new ValidatorFactory().createValidator();
        Object providerNode = new ObjectMapper().readTree(RAW_INSTANCE);
        URI uri = validator.registerSchema(RAW_SCHEMA);
        boolean valid = validator.validate(uri, providerNode).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void validatesInstanceJsonNode() {
        Validator validator = new ValidatorFactory().createValidator();
        JsonNode jsonNode = new JacksonNode.Factory().create(RAW_INSTANCE);
        URI uri = validator.registerSchema(RAW_SCHEMA);
        boolean valid = validator.validate(uri, jsonNode).isValid();
        assertThat(valid).isTrue();
    }

    @Test
    void producesAnnotations() {
        Validator validator = new ValidatorFactory().createValidator();
        String schema = """
                {
                  "title": "hello",
                  "customKeyword": "custom",
                  "another": {}
                }""";
        URI uri = validator.registerSchema(SCHEMA_URI, schema);
        Validator.Result result = validator.validate(uri, RAW_INSTANCE);
        assertThat(result.isValid()).isTrue();
        List<Annotation> annotations = new ArrayList<>(result.getAnnotations());
        annotations.sort(Comparator.comparing(Annotation::getKeyword));
        assertThat(annotations).hasSize(2);
        assertAnnotation(
                annotations.get(0),
                "/customKeyword",
                "urn:test#",
                "",
                "customKeyword",
                "custom"
        );
        assertAnnotation(
                annotations.get(1),
                "/title",
                "urn:test#",
                "",
                "title",
                "hello"
        );
    }

    @Test
    void shouldFallbackToSchemaResolver() {
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("false"))
                .createValidator();

        validator.registerSchema(URI.create("urn:test"), "true");
        Validator.Result result = validator.validate(URI.create("urn:test"), "{}");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:test2"), "{}");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "",
                "urn:test2#",
                "",
                null,
                "False schema always fails"
        );
    }

    @Test
    void allowsEmptyFragmentsInIdRootSchema() {
        // with disabled schema validation
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .createValidator();
        String schema = """
                {
                  "$id": "urn:test#"
                }""";

        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();

        // with enabled schema validation
        validator = new ValidatorFactory().createValidator();

        uri = validator.registerSchema(schema);
        result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void disallowsNonEmptyFragmentsInIdRootSchema() {
        // with disabled schema validation
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .createValidator();

        String schema1 = """
                {
                  "$id": "urn:test#anchor"
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:test#anchor] cannot contain non-empty fragments");

        String schema2 = """
                {
                  "$id": "urn:test#/$defs/x"
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:test#/$defs/x] cannot contain non-empty fragments");

        // with enabled schema validation
        Validator validator2 = new ValidatorFactory().createValidator();

        assertThatThrownBy(() -> validator2.registerSchema(schema1))
                .isInstanceOf(InvalidSchemaException.class)
                .hasMessage("Schema [urn:test#anchor] failed to validate against meta-schema [https://json-schema.org/draft/2020-12/schema]");

        assertThatThrownBy(() -> validator2.registerSchema(schema2))
                .isInstanceOf(InvalidSchemaException.class)
                .hasMessage("Schema [urn:test#/$defs/x] failed to validate against meta-schema [https://json-schema.org/draft/2020-12/schema]");
    }

    @Test
    void allowsEmptyFragmentsInIdSubSchema() {
        // with disabled schema validation
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .createValidator();
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "$id": "urn:sub#"
                    }
                  }
                }""";

        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();

        // with enabled schema validation
        validator = new ValidatorFactory().createValidator();

        uri = validator.registerSchema(schema);
        result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void disallowsNonEmptyFragmentsInIdSubSchema() {
        // with disabled schema validation
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .createValidator();

        String schema1 = """
                {
                  "$defs": {
                    "x": {
                      "$id": "urn:sub#anchor"
                    }
                  }
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:sub#anchor] cannot contain non-empty fragments");

        String schema2 = """
                {
                  "$defs": {
                    "x": {
                      "$id": "urn:sub#/$defs/x"
                    }
                  }
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:sub#/$defs/x] cannot contain non-empty fragments");

        // with enabled schema validation
        Validator validator2 = new ValidatorFactory().createValidator();
        URI uri = URI.create("urn:sub");

        assertThatThrownBy(() -> validator2.registerSchema(uri, schema1))
                .isInstanceOf(InvalidSchemaException.class)
                .hasMessage("Schema [urn:sub] failed to validate against meta-schema [https://json-schema.org/draft/2020-12/schema]");

        assertThatThrownBy(() -> validator2.registerSchema(uri, schema2))
                .isInstanceOf(InvalidSchemaException.class)
                .hasMessage("Schema [urn:sub] failed to validate against meta-schema [https://json-schema.org/draft/2020-12/schema]");
    }
}