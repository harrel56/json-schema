package org.harrel.jsonschema.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.JsonNodeFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

public class JacksonNodeFactory implements JsonNodeFactory {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode wrap(Object node) {
        if (node instanceof com.fasterxml.jackson.databind.JsonNode vendorNode) {
            return new JacksonNode(vendorNode);
        } else {
            throw new IllegalArgumentException("Cannot wrap object which is not an instance of com.fasterxml.jackson.databind.JsonNode");
        }
    }

    @Override
    public JsonNode create(String rawJson) {
        try {
            return new JacksonNode(mapper.readTree(rawJson));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
