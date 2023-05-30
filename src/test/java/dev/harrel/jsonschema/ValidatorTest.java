package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalidUri.toString());
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
        assertThat(annotations.get(0).getKeyword()).isEqualTo("customKeyword");
        assertThat(annotations.get(0).getEvaluationPath()).isEqualTo("/customKeyword");
        assertThat(annotations.get(0).getInstanceLocation()).isEmpty();
        assertThat(annotations.get(0).getSchemaLocation()).isEqualTo("urn:test#");
        assertThat(annotations.get(0).getAnnotation()).isEqualTo("custom");
        assertThat(annotations.get(1).getKeyword()).isEqualTo("title");
        assertThat(annotations.get(1).getEvaluationPath()).isEqualTo("/title");
        assertThat(annotations.get(1).getInstanceLocation()).isEmpty();
        assertThat(annotations.get(1).getSchemaLocation()).isEqualTo("urn:test#");
        assertThat(annotations.get(1).getAnnotation()).isEqualTo("hello");
    }
}