package dev.harrel.jsonschema.util;

import dev.harrel.jsonschema.Annotation;
import dev.harrel.jsonschema.Error;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtil {
    public static String readResource(String resource) {
        try {
            return new String(TestUtil.class.getResourceAsStream(resource).readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<Annotation> sortAnnotations(List<Annotation> annotations) {
        return annotations.stream().sorted(Comparator.comparing(a -> a.getEvaluationPath())).toList();
    }

    public static void assertError(Error error,
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

    public static void assertError(Error error,
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

    public static void assertAnnotation(Annotation annotation,
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
