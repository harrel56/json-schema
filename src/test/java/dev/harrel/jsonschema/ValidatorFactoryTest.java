package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.harrel.jsonschema.TestUtil.readResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorFactoryTest {
    private static final String RAW_SCHEMA = "{\"type\":\"boolean\"}";
    private static final String RAW_INSTANCE = "null";

    @Test
    void name() {
        String schema = readResource("/schema.json");
        String instance = readResource("/instance.json");
        Validator.Result result = new ValidatorFactory().withDefaultMetaSchemaUri(null).validate(schema, instance);
        List<Error> errors = result.getErrors();
        System.out.println(errors);
    }

    @Test
    void emptyEvaluatorFactory() {
        String schema = """
                {
                  "type": "number"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(((ctx, fieldName, fieldNode) -> Optional.empty()))
                .validate(schema, "null");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldFailWhenMetaSchemaCannotBeParsed() {
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("invalid json"))
                .createValidator();
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(MetaSchemaResolvingException.class);
    }

    @Test
    void validateStringString() {
        boolean valid = new ValidatorFactory().validate(RAW_SCHEMA, RAW_INSTANCE).isValid();
        assertThat(valid).isFalse();
    }

    @Test
    void validateProviderNodeString() throws JsonProcessingException {
        Object providerNode = new ObjectMapper().readTree(RAW_SCHEMA);
        boolean valid = new ValidatorFactory().validate(providerNode, RAW_INSTANCE).isValid();
        assertThat(valid).isFalse();
    }

    @Test
    void validateJsonNodeString() {
        JsonNode jsonNode = new JacksonNode.Factory().create(RAW_SCHEMA);
        boolean valid = new ValidatorFactory().validate(jsonNode, RAW_INSTANCE).isValid();
        assertThat(valid).isFalse();
    }

    @Test
    void validateStringProviderNode() throws JsonProcessingException {
        Object providerNode = new ObjectMapper().readTree(RAW_INSTANCE);
        boolean valid = new ValidatorFactory().validate(RAW_SCHEMA, providerNode).isValid();
        assertThat(valid).isFalse();
    }

    @Test
    void validateProviderNodeProviderNode() throws JsonProcessingException {
        Object providerNodeSchema = new ObjectMapper().readTree(RAW_SCHEMA);
        Object providerNodeInstance = new ObjectMapper().readTree(RAW_INSTANCE);
        boolean valid = new ValidatorFactory().validate(providerNodeSchema, providerNodeInstance).isValid();
        assertThat(valid).isFalse();
    }

    @Test
    void validateJsonNodeProviderNode() throws JsonProcessingException {
        JsonNode jsonNode = new JacksonNode.Factory().create(RAW_SCHEMA);
        Object providerNode = new ObjectMapper().readTree(RAW_INSTANCE);
        boolean valid = new ValidatorFactory().validate(jsonNode, providerNode).isValid();
        assertThat(valid).isFalse();
    }

    @Test
    void validateStringJsonNode() {
        JsonNode jsonNode = new JacksonNode.Factory().create(RAW_INSTANCE);
        boolean valid = new ValidatorFactory().validate(RAW_SCHEMA, jsonNode).isValid();
        assertThat(valid).isFalse();
    }

    @Test
    void validateProviderNodeJsonNode() throws JsonProcessingException {
        Object providerNode = new ObjectMapper().readTree(RAW_SCHEMA);
        JsonNode jsonNode = new JacksonNode.Factory().create(RAW_INSTANCE);
        boolean valid = new ValidatorFactory().validate(providerNode, jsonNode).isValid();
        assertThat(valid).isFalse();
    }

    @Test
    void validateJsonNodeJsonNode() {
        JsonNode jsonNodeSchema = new JacksonNode.Factory().create(RAW_SCHEMA);
        JsonNode jsonNodeInstance = new JacksonNode.Factory().create(RAW_INSTANCE);
        boolean valid = new ValidatorFactory().validate(jsonNodeSchema, jsonNodeInstance).isValid();
        assertThat(valid).isFalse();
    }
}