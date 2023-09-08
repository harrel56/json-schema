package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.harrel.jsonschema.TestUtil.assertAnnotation;
import static dev.harrel.jsonschema.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class ValidatorResultTest {

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
    void discardAnnotationCorrectly() {
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
