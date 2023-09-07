package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.harrel.jsonschema.TestUtil.assertAnnotation;
import static dev.harrel.jsonschema.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class ValidatorResultTest {

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
                "Expected object to match at least against one schema. None matched"
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
                "Object didn't match against all schemas. Unmatched schema indexes [1, 2]"
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
                "Object matched against given schema but must not"
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
                "Object matched against more than one schema. Matched schema indexes [0, 1]"
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
