package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.harrel.jsonschema.providers.GsonNode;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static dev.harrel.jsonschema.SchemaResolver.Result;
import static dev.harrel.jsonschema.TestUtil.assertError;
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

    @Test
    void shouldResolveRelativeExternalReferences() {
        String schema1 = """
                {
                  "$id": "a/b/root.json",
                  "type": "object",
                  "properties": {
                    "x": {
                      "$ref": "subschema.json#/$defs/reqFields"
                    }
                  }
                }""";
        String schema2 = """
                {
                  "$id": "a/b/subschema.json",
                  "$defs": {
                    "reqFields": {
                      "required": ["y"]
                    }
                  }
                }""";
        SchemaResolver resolver = uri ->
                switch (uri) {
                    case "a/b/root.json" -> SchemaResolver.Result.fromString(schema1);
                    case "a/b/subschema.json" -> SchemaResolver.Result.fromString(schema2);
                    default -> SchemaResolver.Result.empty();
                };
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(resolver)
                .createValidator();
        validator.registerSchema(schema1);

        String instance1 = """
                {
                  "x": {}
                }""";
        Validator.Result result = validator.validate(URI.create("a/b/root.json"), instance1);

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/properties/x/$ref/required",
                "a/b/subschema.json#/$defs/reqFields",
                "/x",
                "required",
                "Object does not have some of the required properties [[y]]"
        );

        String instance2 = """
                {
                  "x": {
                    "y": null
                  }
                }""";
        result = validator.validate(URI.create("a/b/root.json"), instance2);
        assertThat(result.isValid()).isTrue();
    }
}