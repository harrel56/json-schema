package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorResultTest {
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
        assertThat(errors.get(0).getError()).isEqualTo("Expected hello");
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/const");
        assertThat(errors.get(0).getSchemaLocation()).startsWith("https://harrel.dev/");
        assertThat(errors.get(0).getInstanceLocation()).isEmpty();
        assertThat(errors.get(0).getKeyword()).isEqualTo("const");
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
        assertThat(annotations.get(0).getAnnotation()).isEqualTo("should be retained");
        assertThat(annotations.get(0).getEvaluationPath()).isEqualTo("/title");
        assertThat(annotations.get(0).getSchemaLocation()).startsWith("https://harrel.dev/");
        assertThat(annotations.get(0).getInstanceLocation()).isEmpty();
        assertThat(annotations.get(0).getKeyword()).isEqualTo("title");
    }
}
