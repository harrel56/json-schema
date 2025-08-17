package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class UriUtilTest {
    @ParameterizedTest
    @MethodSource("fragmentReplacements")
    void shouldReplaceUriFragment(URI uri, String fragment, String expected) {
        URI output = UriUtil.getUriWithFragment(uri, fragment);
        assertThat(output).hasToString(expected);
    }

    private static Stream<Arguments> fragmentReplacements() {
        return Stream.of(
                Arguments.of(URI.create("mailto:john.smith@gmail.com"), null, "mailto:john.smith@gmail.com"),
                Arguments.of(URI.create("mailto:john.smith@gmail.com"), "", "mailto:john.smith@gmail.com#"),
                Arguments.of(URI.create("mailto:john.smith@gmail.com"), "/123", "mailto:john.smith@gmail.com#/123"),
                Arguments.of(URI.create("urn:abc:xyz"), "", "urn:abc:xyz#"),
                Arguments.of(URI.create("urn:abc:xyz"), "/123", "urn:abc:xyz#/123"),
                Arguments.of(URI.create("https://harrel.dev"), "", "https://harrel.dev#"),
                Arguments.of(URI.create("https://harrel.dev/"), "/a/b/c", "https://harrel.dev/#/a/b/c"),
                Arguments.of(URI.create("https://harrel.dev/123456789"), "/a/b/c", "https://harrel.dev/123456789#/a/b/c"),
                Arguments.of(URI.create("file:/mnt/file"), "/a/b/c", "file:/mnt/file#/a/b/c"),
                Arguments.of(URI.create("../hmmm"), "/a/b/c", "../hmmm#/a/b/c"),

                // escapes
                Arguments.of(URI.create("urn:test"), "/^que", "urn:test#/%5Eque"),
                Arguments.of(URI.create("urn:test"), "/good morning", "urn:test#/good%20morning"),
                Arguments.of(URI.create("urn:test"), "/%25", "urn:test#/%2525"),
                Arguments.of(URI.create("urn:test"), "/\t\r\n\\", "urn:test#/%09%0D%0A%5C"),
                Arguments.of(URI.create("urn:test"), "/!@#$%^&*()_+-={}[];':\"", "urn:test#/!@%23$%25%5E&*()_+-=%7B%7D[];':%22"),

                // registry-based uri
                Arguments.of(URI.create("http://what:is:this"), "/que", "http://what:is:this#/que"),
                Arguments.of(URI.create("schema:/my_registry;team$project:version/resource"), "/que", "schema:/my_registry;team$project:version/resource#/que")
                );
    }
}