package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.harrel.jsonschema.providers.GsonNode;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import static dev.harrel.jsonschema.SchemaResolver.Result;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchemaResolverTest {
    private static final String SCHEMA = """
            {
              "$ref": "urn:resolver"
            }""";

    @Test
    void shouldFailAlongWithResolver() {
        var error = new RuntimeException();
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> {
                    throw error;
                });
        assertThatThrownBy(() -> validatorFactory.validate(SCHEMA, "{}"))
                .isEqualTo(error);
    }

    @Test
    void shouldResolveFromStringAndBeValid() {
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> Result.fromString("true"));

        var result = validatorFactory.validate(SCHEMA, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldResolveFromStringAndBeInvalid() {
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> Result.fromString("false"));

        var result = validatorFactory.validate(SCHEMA, "{}");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldResolveFromJacksonNodeAndBeValid() throws JsonProcessingException {
        var providerNode = new ObjectMapper().readTree("{}");
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> Result.fromProviderNode(providerNode));

        var result = validatorFactory.validate(SCHEMA, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldResolveFromJacksonNodeAndBeInvalid() throws JsonProcessingException {
        var providerNode = new ObjectMapper().readTree("{\"type\": \"null\"}");
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> Result.fromProviderNode(providerNode));

        var result = validatorFactory.validate(SCHEMA, "{}");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldResolveFromGsonNodeAndBeValid() {
        var providerNode = new Gson().fromJson("true", JsonElement.class);
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withJsonNodeFactory(new GsonNode.Factory())
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> Result.fromProviderNode(providerNode));

        var result = validatorFactory.validate(SCHEMA, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldResolveFromGsonNodeAndBeInvalid() {
        var providerNode = new Gson().fromJson("false", JsonElement.class);
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withJsonNodeFactory(new GsonNode.Factory())
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> Result.fromProviderNode(providerNode));

        var result = validatorFactory.validate(SCHEMA, "{}");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldResolveFromJsonNodeAndBeValid() {
        var jsonNode = new JacksonNode.Factory().create("true");
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> Result.fromJsonNode(jsonNode));

        var result = validatorFactory.validate(SCHEMA, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldResolveFromJsonNodeAndBeInvalid() {
        var jsonNode = new JacksonNode.Factory().create("false");
        ValidatorFactory validatorFactory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> Result.fromJsonNode(jsonNode));

        var result = validatorFactory.validate(SCHEMA, "{}");
        assertThat(result.isValid()).isFalse();
    }
}