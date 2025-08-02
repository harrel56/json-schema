package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.harrel.jsonschema.util.TestUtil.assertAnnotation;
import static dev.harrel.jsonschema.util.TestUtil.assertError;
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
                .isInstanceOf(SchemaNotFoundException.class)
                .hasMessage("Couldn't find schema with uri [https://test.com/x#/$def/x]");
    }

    @Test
    void failsForNonExistentSchemaWithEmptyFragment() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = URI.create("https://test.com/x#");
        assertThatThrownBy(() -> validator.validate(uri, "{}"))
                .isInstanceOf(SchemaNotFoundException.class)
                .hasMessage("Couldn't find schema with uri [https://test.com/x]");
    }

    @Test
    void findsSchemaByGeneratedUri() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = validator.registerSchema("{}");
        Validator.Result result = validator.validate(uri, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void findsSchemaByProvidedUri() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = URI.create("https://test.com/schema");
        validator.registerSchema(URI.create("https://test.com/schema"), "{}");
        Validator.Result result = validator.validate(uri, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void findsSchemaByIdKeyword() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = URI.create("https://test.com/schema/id");
        validator.registerSchema("""
                {
                  "$id": "https://test.com/schema/id"
                }""");
        Validator.Result result = validator.validate(uri, "{}");
        assertThat(result.isValid()).isTrue();
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
    void validatesEmbeddedSchemaAndInstance() {
        JsonNode wrappedSchema = new JacksonNode.Factory().create("""
                {
                  "embedded": %s
                }""".formatted(RAW_SCHEMA));
        JsonNode wrappedInstance = new JacksonNode.Factory().create("""
                {
                  "embedded": true
                }""");

        Validator validator = new ValidatorFactory().createValidator();
        JsonNode jsonNode = wrappedInstance.asObject().get("embedded");
        URI uri = validator.registerSchema(wrappedSchema.asObject().get("embedded"));
        Validator.Result res = validator.validate(uri, jsonNode);
        assertThat(res.isValid()).isFalse();
        assertThat(res.getErrors()).hasSize(1);
        assertThat(res.getErrors().getFirst().getEvaluationPath()).isEqualTo("/not");
        assertThat(res.getErrors().getFirst().getSchemaLocation()).endsWith("#");
        assertThat(res.getErrors().getFirst().getInstanceLocation()).isEqualTo("");
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
    void shouldFallbackToSchemaResolverWithFragment() {
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("""
                        {
                          "$defs": {
                            "sub": {
                              "type": "string"
                            }
                          }
                        }"""))
                .createValidator();

        Validator.Result result = validator.validate(URI.create("urn:test#/$defs/sub"), "{}");
        assertThat(result.isValid()).isFalse();

        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/$defs/sub/type",
                "urn:test#/$defs/sub",
                "",
                "type",
                "Value is [object] but should be [string]"
        );
    }

    @Test
    void shouldFallbackToSchemaResolverWithAnchor() {
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("""
                        {
                          "$defs": {
                            "sub": {
                              "$anchor": "anchor",
                              "type": "string"
                            }
                          }
                        }"""))
                .createValidator();

        Validator.Result result = validator.validate(URI.create("urn:test#anchor"), "{}");
        assertThat(result.isValid()).isFalse();

        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/$defs/sub/type",
                "urn:test#/$defs/sub",
                "",
                "type",
                "Value is [object] but should be [string]"
        );
    }

    @Test
    void shouldFailResolvingExternalSchemaWithInvalidFragment() {
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("""
                        {
                          "$defs": {
                            "sub": {
                              "type": "string"
                            }
                          }
                        }"""))
                .createValidator();

        URI uri = URI.create("urn:test#/$defs/non-existent");
        assertThatThrownBy(() -> validator.validate(uri, "{}"))
                .isInstanceOf(SchemaNotFoundException.class)
                .hasMessage("Couldn't find schema with uri [urn:test#/$defs/non-existent]");
    }

    @Test
    void shouldFailResolvingSchemaWithInvalidFragment() {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = URI.create("urn:test#/$defs/non-existent");
        validator.registerSchema(uri, """
                        {
                          "$defs": {
                            "sub": {
                              "type": "string"
                            }
                          }
                        }""");
        assertThatThrownBy(() -> validator.validate(uri, "{}"))
                .isInstanceOf(SchemaNotFoundException.class)
                .hasMessage("Couldn't find schema with uri [urn:test#/$defs/non-existent]");
    }

    @Test
    void supportsMultipleWaysOfReferencing() {
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "$id": "urn:sub#"
                    }
                  }
                }""";
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .createValidator();

        validator.registerSchema(URI.create("https://test.com/schema"), schema);
        Validator.Result result = validator.validate(URI.create("https://test.com/schema"), "true");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("https://test.com/schema#"), "true");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("https://test.com/schema#/$defs/x"), "true");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:sub"), "true");
        assertThat(result.isValid()).isTrue();

        result = validator.validate(URI.create("urn:sub#"), "true");
        assertThat(result.isValid()).isTrue();
    }
}