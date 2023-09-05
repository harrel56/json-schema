package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.harrel.jsonschema.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class ExhaustiveEvaluationTest {
    @Test
    void prefixItems() {
        String schema = """
                {
                  "prefixItems": [
                    {"const": "a"},
                    {"const": "b"},
                    {"const": "c"},
                    {"const": "d"}
                  ]
                }""";
        String instance = "[0, 1, \"c\", 2]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/prefixItems/0/const",
                "https://harrel.dev/",
                "/0",
                "const",
                "Expected a"
        );
        assertError(
                errors.get(1),
                "/prefixItems/1/const",
                "https://harrel.dev/",
                "/1",
                "const",
                "Expected b"
        );
        assertError(
                errors.get(2),
                "/prefixItems/3/const",
                "https://harrel.dev/",
                "/3",
                "const",
                "Expected d"
        );
    }
}
