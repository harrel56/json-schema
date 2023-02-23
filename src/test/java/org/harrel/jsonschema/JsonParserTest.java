package org.harrel.jsonschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.harrel.jsonschema.validator.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JsonParserTest {

    @Test
    void name() throws IOException {
        String rawSchema = new String(getClass().getResourceAsStream("/schema.json").readAllBytes());
        JacksonNode jacksonNode = new JacksonNode(new ObjectMapper().readTree(rawSchema));
        JsonParser parser = new JsonParser(new ValidatorFactory(), new BasicAnnotationCollector());
        SchemaParsingContext ctx = parser.parseRootSchema("tmp", jacksonNode);
        System.out.println(ctx);
    }
}