package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorResultTest {

    @Test
    void shouldAlwaysReturnExactlyTheSameAnnotationsList() {
        String schema = """
                {
                  "custom": "hello",
                  "title": "world"
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, "{}");

        assertThat(result.isValid()).isTrue();
        // check if lazy getter return always the same instance
        assertThat(result.getAnnotations()).isSameAs(result.getAnnotations());
        List<Annotation> annotations = sortAnnotations(result.getAnnotations());
        assertAnnotation(
                annotations.get(0),
                "/custom",
                "https://harrel.dev/",
                "",
                "custom",
                "hello"
        );
        assertAnnotation(
                annotations.get(1),
                "/title",
                "https://harrel.dev/",
                "",
                "title",
                "world"
        );
    }

    @Test
    void returnsErrorMessageWhenContainsFails() {
        String schema = """
                {
                  "contains": {
                    "type": "null"
                  }
                }""";
        String instance = "[0, 1]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/contains/type",
                "https://harrel.dev/",
                "/0",
                "type",
                "Value is [integer] but should be [null]"
        );
        assertError(
                errors.get(1),
                "/contains/type",
                "https://harrel.dev/",
                "/1",
                "type",
                "Value is [integer] but should be [null]"
        );
        assertError(
                errors.get(2),
                "/contains",
                "https://harrel.dev/",
                "",
                "contains",
                "Array contains no matching items"
        );
    }

    @Test
    void returnsErrorMessageWhenMinContainsFails() {
        String schema = """
                {
                  "contains": {
                    "type": "null"
                  },
                  "minContains": 2
                }""";
        String instance = "[0, 1, null]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/minContains",
                "https://harrel.dev/",
                "",
                "minContains",
                "Array contains less than 2 matching items"
        );
    }

    @Test
    void returnsErrorMessageWhenMaxContainsFails() {
        String schema = """
                {
                  "contains": {
                    "type": "null"
                  },
                  "maxContains": 2
                }""";
        String instance = "[null, null, null]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/maxContains",
                "https://harrel.dev/",
                "",
                "maxContains",
                "Array contains more than 2 matching items"
        );
    }

    @Test
    void returnsErrorMessageWhenDependentSchemasFails() {
        String schema = """
                {
                  "dependentSchemas": {
                    "a": {
                      "type": "object"
                    },
                    "b": {
                      "type": "string"
                    }
                  }
                }""";
        String instance = """
                {
                  "a": 1,
                  "b": null
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
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
                "/dependentSchemas",
                "https://harrel.dev/",
                "",
                "dependentSchemas",
                "Object does not match dependent schemas for some properties [b]"
        );
    }

    @Test
    void returnsErrorMessageWhenIfThenFails() {
        String schema = """
                {
                   "if": true,
                   "then": false,
                   "else": false
                }
                """;

        Validator.Result result = new ValidatorFactory().validate(schema, "null");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getAnnotations()).isEmpty();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(1),
                "/if",
                "https://harrel.dev/",
                "",
                "if",
                "Value matches against schema from 'if' but does not match against schema from 'then'"
        );
    }

    @Test
    void returnsErrorMessageWhenIfElseFails() {
        String schema = """
                {
                   "if": false,
                   "then": false,
                   "else": false
                }
                """;

        Validator.Result result = new ValidatorFactory().validate(schema, "null");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getAnnotations()).isEmpty();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(2),
                "/if",
                "https://harrel.dev/",
                "",
                "if",
                "Value does not match against schema from 'if' and 'else'"
        );
    }


    @Test
    void returnsErrorMessageWhenAnyOfFails() {
        String schema = """
                {
                   "anyOf": [false, false]
                }
                """;

        Validator.Result result = new ValidatorFactory().validate(schema, "null");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getAnnotations()).isEmpty();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(2),
                "/anyOf",
                "https://harrel.dev/",
                "",
                "anyOf",
                "Value does not match against any of the schemas"
        );
    }


    @Test
    void returnsErrorMessageWhenAllOfFails() {
        String schema = """
                {
                   "allOf": [true, false, false, true]
                }
                """;

        Validator.Result result = new ValidatorFactory().validate(schema, "null");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getAnnotations()).isEmpty();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(2),
                "/allOf",
                "https://harrel.dev/",
                "",
                "allOf",
                "Value does not match against the schemas at indexes [1, 2]"
        );
    }

    @Test
    void returnsErrorMessageWhenNotFails() {
        String schema = """
                {
                   "not": true
                }
                """;

        Validator.Result result = new ValidatorFactory().validate(schema, "null");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getAnnotations()).isEmpty();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/not",
                "https://harrel.dev/",
                "",
                "not",
                "Value matches against given schema but it must not"
        );
    }

    @Test
    void returnsErrorMessageWhenOneOfFails() {
        String schema = """
                {
                  "oneOf": [true, true]
                }
                """;

        Validator.Result result = new ValidatorFactory().validate(schema, "null");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getAnnotations()).isEmpty();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/oneOf",
                "https://harrel.dev/",
                "",
                "oneOf",
                "Value matches against more than one schema. Matched schema indexes [0, 1]"
        );
    }

    @Test
    void returnsOnlyDirectErrors() {
        String schema = """
                {
                  "anyOf": [true, false],
                  "const": "hello"
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, "null");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getAnnotations()).isEmpty();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/const",
                "https://harrel.dev/",
                "",
                "const",
                "Expected hello"
        );
    }

    @Test
    void discardsAnnotationCorrectly() {
        String schema = """
                {
                  "anyOf": [true, {
                      "allOf": [false, {
                        "title": "should be discarded"
                      }]
                  }],
                  "title": "should be retained"
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, "null");
        assertThat(result.isValid()).isTrue();
        List<Annotation> annotations = result.getAnnotations();
        assertThat(annotations).hasSize(1);
        assertAnnotation(
                annotations.get(0),
                "/title",
                "https://harrel.dev/",
                "",
                "title",
                "should be retained"
        );
    }
}
