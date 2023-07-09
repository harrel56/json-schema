package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static dev.harrel.jsonschema.TestUtil.readResource;
import static org.assertj.core.api.Assertions.*;

class ValidatorFactoryTest {
    private static final String RAW_SCHEMA = "{\"type\":\"boolean\"}";
    private static final String RAW_INSTANCE = "null";

    @Test
    void name() {
        String schema = readResource("/schema.json");
        String instance = readResource("/instance.json");
        Validator.Result result = new ValidatorFactory().withDisabledSchemaValidation(true).validate(schema, instance);
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
                .withDialect(new Dialects.Draft2020Dialect() {
                    @Override
                    public EvaluatorFactory getEvaluatorFactory() {
                        return (ctx, fieldName, fieldNode) -> Optional.empty();
                    }
                })
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
    void shouldFailWhenSchemaValidationFails() {
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2020Dialect() {
                    @Override
                    public String getMetaSchema() {
                        return null;
                    }
                })
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString(RAW_SCHEMA))
                .createValidator();
        InvalidSchemaException e = catchThrowableOfType(() -> validator.registerSchema(schema), InvalidSchemaException.class);
        assertThat(e.getErrors()).hasSize(1);
        assertThat(e.getErrors().get(0).getKeyword()).isEqualTo("type");
        assertThat(e.getErrors().get(0).getEvaluationPath()).isEqualTo("/type");
        assertThat(e.getErrors().get(0).getInstanceLocation()).isEmpty();
        assertThat(e.getErrors().get(0).getSchemaLocation()).isEqualTo("urn:meta#");
        assertThat(e.getErrors().get(0).getError()).isEqualTo("Value is [object] but should be [boolean]");
    }

    @Test
    void shouldFailForUnresolvableRef() {
        String schema = """
                {
                  "$ref": "urn:missing"
                }""";
        boolean valid = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .validate(schema, RAW_INSTANCE)
                .isValid();
        assertThat(valid).isFalse();
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

    @Test
    void refToExternalSubSchemaPasses() {
        String schema = """
                {
                  "$ref": "urn:x#/nope"
                }""";
        String refSchema = """
                {
                  "nope": {
                    "const": true
                  }
                }""";
        Validator.Result result = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString(refSchema))
                .validate(schema, "true");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void refToExternalMissingSubSchemaFails() {
        String schema = """
                {
                  "$ref": "urn:x#/nope"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("{}"))
                .validate(schema, "true");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getKeyword()).isEqualTo("$ref");
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/$ref");
        assertThat(errors.get(0).getInstanceLocation()).isEmpty();
        assertThat(errors.get(0).getError()).isEqualTo("Resolution of $ref [urn:x#/nope] failed");
    }

    @Test
    void metaRefToExternalSubSchemaPasses() {
        String schema = """
                {
                  "$schema": "urn:x#/nope"
                }""";
        String refSchema = """
                {
                  "nope": {
                    "type": "object"
                  }
                }""";
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString(refSchema))
                .createValidator();
        URI uri = validator.registerSchema(schema);
        assertThat(validator.validate(uri, "true").isValid()).isTrue();
    }

    @Test
    void metaRefToExternalMissingSubSchemaPasses() {
        String schema = """
                {
                  "$schema": "urn:x#/nope"
                }""";
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("{}"))
                .createValidator();
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(MetaSchemaResolvingException.class)
                .hasMessage("Cannot resolve meta-schema [urn:x#/nope]");
    }

    @Test
    void shouldCombineUserProvidedEvaluatorFactory() {
        Evaluator customEvaluator = new Evaluator() {
            @Override
            public Result evaluate(EvaluationContext ctx, JsonNode node) {
                return Evaluator.Result.failure("custom error");
            }

            @Override
            public int getOrder() {
                return 10;
            }
        };
        EvaluatorFactory customFactory = (ctx, fieldName, fieldNode) -> {
            if ("type".equals(fieldName)) {
                return Optional.of(customEvaluator);
            } else {
                return Optional.empty();
            }
        };
        String schema = """
                {
                  "type": "string",
                  "minLength": 2
                }""";
        Validator.Result result = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withEvaluatorFactory(customFactory)
                .validate(schema, "\"x\"");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors().get(0).getError()).isEqualTo("\"x\" is shorter than 2 characters");
        assertThat(result.getErrors().get(1).getError()).isEqualTo("custom error");
    }

    @Disabled("To be enabled when thread safety issues are taken care of")
    @RepeatedTest(500)
    void threadSafetyRegistrationTest() {
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("true"))
                .createValidator();
        IntStream.range(0, 100).parallel().forEach(i -> validator.registerSchema(schema));
        URI uri = validator.registerSchema(schema);
        assertThat(validator.validate(uri, "true").isValid()).isTrue();
    }
}