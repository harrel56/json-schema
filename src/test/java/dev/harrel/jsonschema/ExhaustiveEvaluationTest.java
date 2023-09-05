package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.harrel.jsonschema.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class ExhaustiveEvaluationTest {
    @Test
    void prefixItems() {
        String schema = """
                {
                  "prefixItems": [
                    {"const": "a"},
                    {"const": "b"},
                    {"const": "c"},
                    {"const": "d"}
                  ]
                }""";
        String instance = "[0, 1, \"c\", 2]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/prefixItems/0/const",
                "https://harrel.dev/",
                "/0",
                "const",
                "Expected a"
        );
        assertError(
                errors.get(1),
                "/prefixItems/1/const",
                "https://harrel.dev/",
                "/1",
                "const",
                "Expected b"
        );
        assertError(
                errors.get(2),
                "/prefixItems/3/const",
                "https://harrel.dev/",
                "/3",
                "const",
                "Expected d"
        );
    }

    @Test
    void items() {
        String schema = """
                {
                  "items": {
                    "type": "number"
                  }
                }""";
        String instance = "[null, 0, \"a\", true]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/items/type",
                "https://harrel.dev/",
                "/0",
                "type",
                "Value is [null] but should be [number]"
        );
        assertError(
                errors.get(1),
                "/items/type",
                "https://harrel.dev/",
                "/2",
                "type",
                "Value is [string] but should be [number]"
        );
        assertError(
                errors.get(2),
                "/items/type",
                "https://harrel.dev/",
                "/3",
                "type",
                "Value is [boolean] but should be [number]"
        );
    }

    @Test
    void additionalProperties() {
        String schema = """
                {
                  "properties": {
                    "b": {
                      "type": "boolean"
                    }
                  },
                  "additionalProperties": {
                    "type": "number"
                  }
                }""";
        String instance = """
                {
                  "a": null,
                  "b": true,
                  "c": "prop",
                  "d": 0
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/additionalProperties/type",
                "https://harrel.dev/",
                "/a",
                "type",
                "Value is [null] but should be [number]"
        );
        assertError(
                errors.get(1),
                "/additionalProperties/type",
                "https://harrel.dev/",
                "/c",
                "type",
                "Value is [string] but should be [number]"
        );
    }

    @Test
    void properties() {
        String schema = """
                {
                  "properties": {
                    "a": {
                      "type": "boolean"
                    },
                    "b": {
                      "type": "boolean"
                    },
                    "c": {
                      "type": "boolean"
                    },
                    "d": {
                      "type": "boolean"
                    }
                  }
                }""";
        String instance = """
                {
                  "a": true,
                  "b": null,
                  "c": "prop",
                  "d": 0
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/properties/b/type",
                "https://harrel.dev/",
                "/b",
                "type",
                "Value is [null] but should be [boolean]"
        );
        assertError(
                errors.get(1),
                "/properties/c/type",
                "https://harrel.dev/",
                "/c",
                "type",
                "Value is [string] but should be [boolean]"
        );
        assertError(
                errors.get(2),
                "/properties/d/type",
                "https://harrel.dev/",
                "/d",
                "type",
                "Value is [integer] but should be [boolean]"
        );
    }
}
