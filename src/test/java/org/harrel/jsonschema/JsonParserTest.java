package org.harrel.jsonschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

class JsonParserTest {

    @Test
    void name() throws IOException {
        String rawSchema = new String(getClass().getResourceAsStream("/schema.json").readAllBytes());
        JacksonNode jacksonNode = new JacksonNode(new ObjectMapper().readTree(rawSchema));
        JsonParser parser = new JsonParser(new ValidatorFactory(), new BasicValidationCollector());
        SchemaParsingContext ctx = parser.parse(URI.create("tmp"), jacksonNode);
        System.out.println(ctx);
    }
}