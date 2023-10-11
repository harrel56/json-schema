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
    void shouldResolveRelativeExternalRefs() {
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

    @Test
    void shouldResolveExternalRefsChain() {
        String schemaX = """
                {
                  "$id": "a/b/x",
                  "$ref": "y#/$defs/y"
                }""";
        String schemaY = """
                {
                  "$id": "a/b/y",
                  "$defs": {
                    "y": {
                      "$ref": "z#/$defs/z"
                    }
                  }
                }""";
        String schemaZ = """
                {
                  "$id": "a/b/z",
                  "$defs": {
                    "z": {
                      "$ref": "https://external.com/a/b/c#/$defs/c"
                    }
                  }
                }""";
        String schemaC = """
                {
                  "$id": "https://external.com/a/b/c",
                  "$defs": {
                    "c": {
                      "$ref": "/a/b#/$defs/b"
                    }
                  }
                }""";
        String schemaB = """
                {
                  "$id": "https://external.com/a/b",
                  "$defs": {
                    "b": {
                      "$ref": "d#/$defs/d"
                    }
                  }
                }""";
        String schemaD = """
                {
                  "$id": "https://external.com/a/d",
                  "$defs": {
                    "d": {
                      "type": "string"
                    }
                  }
                }""";
        SchemaResolver resolver = uri ->
                switch (uri) {
                    case "a/b/x" -> SchemaResolver.Result.fromString(schemaX);
                    case "a/b/y" -> SchemaResolver.Result.fromString(schemaY);
                    case "a/b/z" -> SchemaResolver.Result.fromString(schemaZ);
                    case "https://external.com/a/b/c" -> SchemaResolver.Result.fromString(schemaC);
                    case "https://external.com/a/b" -> SchemaResolver.Result.fromString(schemaB);
                    case "https://external.com/a/d" -> SchemaResolver.Result.fromString(schemaD);
                    default -> SchemaResolver.Result.empty();
                };

        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(resolver)
                .createValidator();
        validator.registerSchema(schemaX);

        String instance1 = "1";
        Validator.Result result = validator.validate(URI.create("a/b/x"), instance1);

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/$ref/$ref/$ref/$ref/$ref/type",
                "https://external.com/a/d#/$defs/d",
                "",
                "type",
                "Value is [integer] but should be [string]"
        );
    }
}