package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.harrel.jsonschema.TestUtil.assertAnnotation;
import static dev.harrel.jsonschema.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class ValidatorResultTest {

    @Test
    void returnsErrorMessageWhenIncorrect() {
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
                "Must be only valid against one of the subschemas"
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
