package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.harrel.jsonschema.TestUtil.readResource;
import static org.junit.jupiter.api.Assertions.*;

class ValidatorFactoryTest {

    @Test
    void name() throws JsonProcessingException {
        String schema = readResource("/schema.json");
        String instance = readResource("/instance.json");
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        System.out.println(new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .writeValueAsString(result.getValidationAnnotations()));
        List<Annotation> errors = result.getErrors();
        System.out.println(errors);
    }
}