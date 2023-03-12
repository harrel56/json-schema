package org.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.harrel.jsonschema.validator.ValidatorFactory;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class SchemaValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchemaResolver schemaResolver;
    private final JsonParser jsonParser;

    public SchemaValidator() {
        this(new JacksonNodeFactory(), uri -> Optional.empty());
    }

    public SchemaValidator(JsonNodeFactory jsonNodeFactory, SchemaResolver schemaResolver) {
        this.schemaResolver = schemaResolver;
        this.jsonParser = new JsonParser(jsonNodeFactory, new ValidatorFactory(), new SchemaRegistry());
    }

    public boolean validate(String rawSchema, String rawJson) throws JsonProcessingException {
        JacksonNode schema = new JacksonNode(objectMapper.readTree(rawSchema));
        JacksonNode json = new JacksonNode(objectMapper.readTree(rawJson));
        return validate(schema, json);
    }

    public boolean validate(JsonNode schema, JsonNode json) {
        String generatedUri = UUID.randomUUID().toString();
        SchemaParsingContext ctx = jsonParser.parseRootSchema(generatedUri, schema);
        return ctx.validateSchema(jsonParser, schemaResolver, json);
    }

    public void registerSchema(URI uri, JsonNode schema) {
        jsonParser.parseRootSchema(uri.toString(), schema);
    }
}
