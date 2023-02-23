package org.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.harrel.jsonschema.validator.ValidatorFactory;

import java.util.UUID;

public class SchemaValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ValidatorFactory validatorFactory = new ValidatorFactory();

    public boolean validate(String rawSchema, String rawJson) throws JsonProcessingException {
        JacksonNode schema = new JacksonNode(objectMapper.readTree(rawSchema));
        JacksonNode json = new JacksonNode(objectMapper.readTree(rawJson));
        return validate(schema, json);
    }

    public boolean validate(JsonNode schema, JsonNode json) {
        BasicAnnotationCollector collector = new BasicAnnotationCollector();
        JsonParser parser = new JsonParser(validatorFactory, collector);
        String generatedUri = UUID.randomUUID().toString();
        SchemaParsingContext ctx = parser.parseRootSchema(generatedUri, schema);
        return ctx.validateSchema(collector, json);
    }
}
