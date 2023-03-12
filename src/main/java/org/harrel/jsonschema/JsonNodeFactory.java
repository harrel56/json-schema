package org.harrel.jsonschema;

import java.io.IOException;

public interface JsonNodeFactory {
    JsonNode create(String rawJson) throws IOException;
}
