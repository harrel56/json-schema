package org.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonNodeFactory implements JsonNodeFactory {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode create(String rawJson) throws JsonProcessingException {
        return new JacksonNode(mapper.readTree(rawJson));
    }
}
