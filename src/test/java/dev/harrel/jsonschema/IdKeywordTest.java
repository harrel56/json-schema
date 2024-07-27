package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdKeywordTest {
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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:test#anchor] cannot contain non-empty fragments");

        assertThatThrownBy(() -> validator2.registerSchema(schema2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:test#/$defs/x] cannot contain non-empty fragments");
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
