package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.harrel.jsonschema.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicatorTest {
    @Test
    void contains() {
        String schema = """
                {
                  "contains": {
                    "type": "null"
                  }
                }""";
        String instance = "[0, 1]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(3);
        assertError(
                errors.get(0),
                "/contains/type",
                "https://harrel.dev/",
                "/0",
                "type",
                "Value is [integer] but should be [null]"
        );
        assertError(
                errors.get(1),
                "/contains/type",
                "https://harrel.dev/",
                "/1",
                "type",
                "Value is [integer] but should be [null]"
        );
        assertError(
                errors.get(2),
                "/contains",
                "https://harrel.dev/",
                "",
                "contains",
                "No items match contains"
        );
    }

    @Test
    void minContains() {
        String schema = """
                {
                  "contains": {
                    "type": "null"
                  },
                  "minContains": 2
                }""";
        String instance = "[0, 1, null]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/minContains",
                "https://harrel.dev/",
                "",
                "minContains",
                "Array contains less than 2 matching items"
        );
    }

    @Test
    void maxContains() {
        String schema = """
                {
                  "contains": {
                    "type": "null"
                  },
                  "maxContains": 2
                }""";
        String instance = "[null, null, null]";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/maxContains",
                "https://harrel.dev/",
                "",
                "maxContains",
                "Array contains more than 2 matching items"
        );
    }

    @Test
    void dependentSchemas() {
        String schema = """
                {
                  "dependentSchemas": {
                    "a": {
                      "type": "object"
                    },
                    "b": {
                      "type": "string"
                    }
                  }
                }""";
        String instance = """
                {
                  "a": 1,
                  "b": null
                }""";
        Validator.Result result = new ValidatorFactory().validate(schema, instance);
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/dependentSchemas/b/type",
                "https://harrel.dev/",
                "",
                "type",
                "Value is [object] but should be [string]"
        );
        assertError(
                errors.get(1),
                "/dependentSchemas",
                "https://harrel.dev/",
                "",
                "dependentSchemas",
                "Dependent schema validation failed for some properties [b]"
        );
    }
}
