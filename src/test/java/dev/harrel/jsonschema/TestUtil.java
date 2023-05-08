package dev.harrel.jsonschema;

import java.io.IOException;
import java.io.UncheckedIOException;

class TestUtil {
    static String readResource(String resource) {
        try {
            return new String(SpecificationTest.class.getResourceAsStream(resource).readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
