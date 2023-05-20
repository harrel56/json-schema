package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.harrel.jsonschema.TestUtil.readResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorFactoryTest {

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
}