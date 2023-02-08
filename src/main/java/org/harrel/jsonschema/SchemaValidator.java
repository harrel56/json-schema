package org.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.harrel.jsonschema.validator.ValidatorFactory;

import java.net.URI;

public class SchemaValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ValidatorFactory validatorFactory = new ValidatorFactory();

    public boolean validate(String rawSchema, String rawJson) throws JsonProcessingException {
        JacksonNode schema = new JacksonNode(objectMapper.readTree(rawSchema));
        JacksonNode json = new JacksonNode(objectMapper.readTree(rawJson));
        return validate(schema, json);
    }

    public boolean validate(JacksonNode schema, JacksonNode json) {
        BasicValidationCollector collector = new BasicValidationCollector();
        JsonParser parser = new JsonParser(validatorFactory, collector);
        URI uri = URI.create("tmp");
        SchemaParsingContext ctx = parser.parse(uri, schema);
        return ctx.validateSchema(uri.toString(), json);
    }
}
