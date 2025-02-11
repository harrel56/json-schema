package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
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

    @Test
    void patternProperties() {
        String schema = """
                {
                  "patternProperties": {
                    "^a.*z$": {
                      "type": "string"
                    },
                     "^az$": {
                      "type": "string"
                    },
                    "^z.*a$": {
                      "type": "number"
                    }
                  }
                }""";
        String instance = """
                {
                  "a---z": true,
                  "az": true,
                  "a---b": null,
                  "z---a": "prop",
                  "zxa": null,
                  "zxxxxa": 0
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors().stream()
                .sorted(Comparator.comparing(Error::getEvaluationPath).thenComparing(Error::getInstanceLocation))
                .toList();
        assertThat(errors).hasSize(5);
        assertError(
                errors.get(0),
                "/patternProperties/^a.*z$/type",
                "https://harrel.dev/",
                "/a---z",
                "type",
                "Value is [boolean] but should be [string]"
        );
        assertError(
                errors.get(1),
                "/patternProperties/^a.*z$/type",
                "https://harrel.dev/",
                "/az",
                "type",
                "Value is [boolean] but should be [string]"
        );
        assertError(
                errors.get(2),
                "/patternProperties/^az$/type",
                "https://harrel.dev/",
                "/az",
                "type",
                "Value is [boolean] but should be [string]"
        );
        assertError(
                errors.get(3),
                "/patternProperties/^z.*a$/type",
                "https://harrel.dev/",
                "/z---a",
                "type",
                "Value is [string] but should be [number]"
        );
        assertError(
                errors.get(4),
                "/patternProperties/^z.*a$/type",
                "https://harrel.dev/",
                "/zxa",
                "type",
                "Value is [null] but should be [number]"
        );

    }

    @Test
    void dependentSchemas() {
        String schema = """
                {
                  "dependentSchemas": {
                    "a": {
                      "type": "object"
                    },
                    "b": {
                      "type": "string"
                    },
                    "c": {
                      "type": "string"
                    }
                  }
                }""";
        String instance = """
                {
                  "a": 1,
                  "b": null,
                  "c": null
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/dependentSchemas/b/type",
                "https://harrel.dev/",
                "",
                "type",
                "Value is [object] but should be [string]"
        );
        assertError(
                errors.get(1),
                "/dependentSchemas/c/type",
                "https://harrel.dev/",
                "",
                "type",
                "Value is [object] but should be [string]"
        );
        assertError(
                errors.get(2),
                "/dependentSchemas",
                "https://harrel.dev/",
                "",
                "dependentSchemas",
                "Object does not match dependent schemas for some properties [b, c]"
        );
    }

    @Test
    void propertyNames() {
        String schema = """
                {
                  "propertyNames": {
                    "maxLength": 2
                  }
                }""";
        String instance = """
                {
                  "a": true,
                  "aa": null,
                  "aaa": 1,
                  "aaaa": 2
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/propertyNames/maxLength",
                "https://harrel.dev/",
                "",
                "maxLength",
                "\"aaa\" is longer than 2 characters"
        );
        assertError(
                errors.get(1),
                "/propertyNames/maxLength",
                "https://harrel.dev/",
                "",
                "maxLength",
                "\"aaaa\" is longer than 2 characters"
        );
    }

    @Test
    void unevaluatedProperties() {
        String schema = """
                {
                  "properties": {
                    "a": true,
                    "b": true
                  },
                  "unevaluatedProperties": {
                    "type": "number",
                    "maximum": 10
                  }
                }""";
        String instance = """
                {
                  "a": 11,
                  "b": 12,
                  "c": 13,
                  "d": 14,
                  "e": null
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/unevaluatedProperties/maximum",
                "https://harrel.dev/",
                "/c",
                "maximum",
                "13 is greater than 10"
        );
        assertError(
                errors.get(1),
                "/unevaluatedProperties/maximum",
                "https://harrel.dev/",
                "/d",
                "maximum",
                "14 is greater than 10"
        );
        assertError(
                errors.get(2),
                "/unevaluatedProperties/type",
                "https://harrel.dev/",
                "/e",
                "type",
                "Value is [null] but should be [number]"
        );
    }

    @Test
    void unevaluatedItems() {
        String schema = """
                {
                  "prefixItems": [true, true],
                  "unevaluatedItems": {
                    "minLength": 2
                  }
                }""";
        String instance = "[0, \"a\", \"a\", \"a\"]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/unevaluatedItems/minLength",
                "https://harrel.dev/",
                "/2",
                "minLength",
                "\"a\" is shorter than 2 characters"
        );
        assertError(
                errors.get(1),
                "/unevaluatedItems/minLength",
                "https://harrel.dev/",
                "/3",
                "minLength",
                "\"a\" is shorter than 2 characters"
        );
    }

    @Test
    void unevaluatedItemsWithPrefixItemsErrors() {
        String schema = """
                {
                  "prefixItems": [true, false],
                  "unevaluatedItems": {
                    "minLength": 2
                  }
                }""";
        String instance = "[\"a\", \"a\", \"a\"]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/prefixItems/1",
                "https://harrel.dev/",
                "/1",
                null,
                "False schema always fails"
        );
        assertError(
                errors.get(1),
                "/unevaluatedItems/minLength",
                "https://harrel.dev/",
                "/2",
                "minLength",
                "\"a\" is shorter than 2 characters"
        );
    }

    @Test
    void unevaluatedItemsWithItemsErrors() {
        String schema = """
                {
                  "items": {
                    "const": "b"
                  },
                  "unevaluatedItems": {
                    "minLength": 2
                  }
                }""";
        String instance = "[\"a\", \"a\"]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/items/const",
                "https://harrel.dev/",
                "/0",
                "const",
                "Expected b"
        );
        assertError(
                errors.get(1),
                "/items/const",
                "https://harrel.dev/",
                "/1",
                "const",
                "Expected b"
        );
    }

    @Test
    void unevaluatedItemsWithLegacyItemsErrors() {
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2019-09/schema",
                  "items": [true, false],
                  "unevaluatedItems": {
                    "minLength": 2
                  }
                }""";
        String instance = "[\"a\", \"a\", \"a\"]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/items/1",
                "https://harrel.dev/",
                "/1",
                null,
                "False schema always fails"
        );
        assertError(
                errors.get(1),
                "/unevaluatedItems/minLength",
                "https://harrel.dev/",
                "/2",
                "minLength",
                "\"a\" is shorter than 2 characters"
        );
    }

    @Test
    void unevaluatedItemsWithLegacyItems2Errors() {
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2019-09/schema",
                  "items": {
                    "const": "b"
                  },
                  "unevaluatedItems": {
                    "minLength": 2
                  }
                }""";
        String instance = "[\"a\", \"a\"]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/items/const",
                "https://harrel.dev/",
                "/0",
                "const",
                "Expected b"
        );
        assertError(
                errors.get(1),
                "/items/const",
                "https://harrel.dev/",
                "/1",
                "const",
                "Expected b"
        );
    }

    @Test
    void unevaluatedItemsWithAdditionalItemsErrors() {
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2019-09/schema",
                  "items": [false],
                  "additionalItems": false,
                  "unevaluatedItems": {
                    "minLength": 2
                  }
                }""";
        String instance = "[\"a\", \"a\"]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/items/0",
                "https://harrel.dev/",
                "/0",
                null,
                "False schema always fails"
        );
        assertError(
                errors.get(1),
                "/additionalItems",
                "https://harrel.dev/",
                "/1",
                null,
                "False schema always fails"
        );
    }

    @Test
    void unevaluatedPropertiesWithEvaluatedErrors() {
        String schema = """
                {
                  "properties": {
                    "a": true,
                    "b": false
                  },
                  "unevaluatedProperties": {
                    "minLength": 2
                  }
                }""";
        String instance = """
                {
                  "a": "a",
                  "b": "b",
                  "c": "c",
                  "d": "d"
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/properties/b",
                "https://harrel.dev/",
                "/b",
                null,
                "False schema always fails"
        );
        assertError(
                errors.get(1),
                "/unevaluatedProperties/minLength",
                "https://harrel.dev/",
                "/c",
                "minLength",
                "\"c\" is shorter than 2 characters"
        );
        assertError(
                errors.get(2),
                "/unevaluatedProperties/minLength",
                "https://harrel.dev/",
                "/d",
                "minLength",
                "\"d\" is shorter than 2 characters"
        );
    }
}
