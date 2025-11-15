package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorTest {
    @Test
    void overrideToString() {
        Error err = new Error("/xy/z", "https://harrel.dev/123#/xy/z",
                "/a/b/3/c", "custom", "This is invalid.");
        assertThat(err).hasToString("Error{keyword=custom, evaluationPath=/xy/z, schemaLocation=https://harrel.dev/123#/xy/z, " +
                "instanceLocation=/a/b/3/c, error=This is invalid.}");
    }
}