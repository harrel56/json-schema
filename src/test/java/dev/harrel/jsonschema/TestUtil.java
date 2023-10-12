package dev.harrel.jsonschema;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;

class TestUtil {
    static String readResource(String resource) {
        try {
            return new String(SpecificationTest.class.getResourceAsStream(resource).readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void assertError(Error error,
                            String evaluationPath,
                            String schemaLocation,
                            String instanceLocation,
                            String keyword) {
        assertThat(error.getEvaluationPath()).isEqualTo(evaluationPath);
        assertThat(error.getSchemaLocation()).startsWith(schemaLocation);
        assertThat(error.getInstanceLocation()).isEqualTo(instanceLocation);
        assertThat(error.getKeyword()).isEqualTo(keyword);
        assertThat(error.getError()).isNotNull();
    }

    static void assertError(Error error,
                            String evaluationPath,
                            String schemaLocation,
                            String instanceLocation,
                            String keyword,
                            String errorMessage) {
        assertThat(error.getEvaluationPath()).isEqualTo(evaluationPath);
        assertThat(error.getSchemaLocation()).startsWith(schemaLocation);
        assertThat(error.getInstanceLocation()).isEqualTo(instanceLocation);
        assertThat(error.getKeyword()).isEqualTo(keyword);
        assertThat(error.getError()).isEqualTo(errorMessage);
    }

    static void assertAnnotation(Annotation annotation,
                                 String evaluationPath,
                                 String schemaLocation,
                                 String instanceLocation,
                                 String keyword,
                                 Object annotationObject) {
        assertThat(annotation.getEvaluationPath()).isEqualTo(evaluationPath);
        assertThat(annotation.getSchemaLocation()).startsWith(schemaLocation);
        assertThat(annotation.getInstanceLocation()).isEqualTo(instanceLocation);
        assertThat(annotation.getKeyword()).isEqualTo(keyword);
        assertThat(annotation.getAnnotation()).isEqualTo(annotationObject);
    }
}
