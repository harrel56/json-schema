package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EvalStateTest {
    @Test
    void shouldInitWithNoAnnotations() {
        EvalState state = new EvalState(URI.create("https://example.com"), 0);
        assertThat(state.getSiblingAnnotation(Keyword.DEPRECATED)).isNull();
        assertThat(state.getSiblingAnnotation("")).isNull();
        assertThat(state.getSiblingAnnotation("anything")).isNull();
        assertThat(state.getSiblingAnnotation("hmm")).isNull();
    }

    @ParameterizedTest
    @MethodSource("keywords")
    void shouldSupportAllStandardKeywords(String keyword) {
        EvalState state = new EvalState(URI.create("https://example.com"), 0);
        Annotation annotation = new Annotation("", "", "", "", "");
        state.setSiblingAnnotation(keyword, annotation);
        assertThat(state.getSiblingAnnotation(keyword)).isEqualTo(annotation);
    }

    private static Stream<String> keywords() {
        return Arrays.stream(Keyword.class.getFields())
                .map(field -> {
                    try {
                        return field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .filter(String.class::isInstance)
                .map(String.class::cast);
    }
}