package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationTest {
    @Test
    void overrideToString() {
        Annotation anno = new Annotation("/xy/z", "https://harrel.dev/123#/xy/z",
                "/a/b/3/c", "title", List.of("a", List.of("b", "c")));
        assertThat(anno).hasToString("Annotation{keyword=title, evaluationPath=/xy/z, schemaLocation=https://harrel.dev/123#/xy/z, " +
                "instanceLocation=/a/b/3/c, annotation=[a, [b, c]]}");
    }
}