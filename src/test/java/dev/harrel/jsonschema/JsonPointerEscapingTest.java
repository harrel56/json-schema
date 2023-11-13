package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.URI;
import java.util.List;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class JsonPointerEscapingTest implements ProviderTest {

    @Test
    void shouldEncodeJsonObjects() {
        String json = """
                {
                  "x": true,
                  "x/y": true,
                  "x/y/z": true,
                  "x////y": true,
                  "/": true,
                  "~": true,
                  "~0~1": true,
                  "a/b/c": {
                    "d/e/f": {
                      "g/h": true
                    }
                  }
                }""";
        JsonNode jsonNode = getJsonNodeFactory().create(json);
        assertThat(jsonNode.isObject()).isTrue();
        assertThat(jsonNode.asObject()).containsOnlyKeys("x", "x~1y", "x~1y~1x", "x~1~1~1~1y", "~1", "~0", "~00~01", "a~1b~1c");
        JsonNode nested1 = jsonNode.asObject().get("a~1b~1c");
        assertThat(nested1.isObject()).isTrue();
        assertThat(nested1.asObject()).containsOnlyKeys("d~1e~1f");
        JsonNode nested2 = jsonNode.asObject().get("d~1e~1f");
        assertThat(nested2.isObject()).isTrue();
        assertThat(nested2.asObject()).containsOnlyKeys("g~1h");
        assertThat(nested2.asObject().get("g~1h").getJsonPointer()).isEqualTo("a~1b~1c/d~1e~1f/g~1h");
    }

    @Test
    void shouldEncodeComplexJsonPointers() {
        String json = """
                [
                  {
                    "/a/": [
                      [],
                      [],
                      [
                        {
                          "/b/c~": {
                            "~/": [
                              {
                                "nested": true
                              }
                            ]
                          }
                        }
                      ]
                    ]
                  }
                ]""";
        JsonNode jsonNode = getJsonNodeFactory().create(json);
        JsonNode nestedNode = jsonNode
                .asArray().get(0)
                .asObject().get("~1a~1")
                .asArray().get(3)
                .asArray().get(0)
                .asObject().get("~1b~1c~0")
                .asObject().get("~0~1")
                .asArray().get(0)
                .asObject().get("nested");

        assertThat(nestedNode.isBoolean()).isTrue();
        assertThat(nestedNode.getJsonPointer()).isEqualTo("/0/~1a~1/3/0/~1b~1c~0/~0~1/0/nested");
    }

    @Test
    void shouldEscapeWhenRegisteringSchemas1() {
        // TODO add 3 other variants
        String schema = """
                {
                  "$ref": "#/$defs/x/y",
                  "$defs": {
                    "x/y": true,
                    "x": {
                      "y": false
                    }
                  }
                }""";
        Validator validator = new ValidatorFactory().withDisabledSchemaValidation(true).createValidator();
        URI uri = validator.registerSchema(schema);

        Validator.Result result = validator.validate(uri, "{}");

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "",
                "",
                "",
                null,
                "Object does not have some of the required properties [[y]]"
        );
    }
}
