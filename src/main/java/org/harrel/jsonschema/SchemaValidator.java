package org.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;

public class SchemaValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ValidatorFactory validatorFactory = new ValidatorFactory();

    public boolean validate(String rawSchema, String rawJson) throws JsonProcessingException {
        JsonParser parser = new JsonParser(validatorFactory);
        URI uri = URI.create("tmp");
        JacksonNode schema = new JacksonNode(objectMapper.readTree(rawSchema));
        JacksonNode json = new JacksonNode(objectMapper.readTree(rawJson));
        SchemaParsingContext ctx = parser.parse(uri, schema);
        return ctx.validateSchema(uri.toString(), json);
    }
}
