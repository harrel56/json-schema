package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
        Map<String, JsonNode> object1 = jsonNode.asObject();
        assertThat(object1.get("x").getJsonPointer()).isEqualTo("/x");
        assertThat(object1.get("x/y").getJsonPointer()).isEqualTo("/x~1y");
        assertThat(object1.get("x/y/z").getJsonPointer()).isEqualTo("/x~1y~1z");
        assertThat(object1.get("x////y").getJsonPointer()).isEqualTo("/x~1~1~1~1y");
        assertThat(object1.get("/").getJsonPointer()).isEqualTo("/~1");
        assertThat(object1.get("~").getJsonPointer()).isEqualTo("/~0");
        assertThat(object1.get("~0~1").getJsonPointer()).isEqualTo("/~00~01");
        assertThat(object1.get("a/b/c").getJsonPointer()).isEqualTo("/a~1b~1c");

        Map<String, JsonNode> nested1 = object1.get("a/b/c").asObject();
        assertThat(nested1.get("d/e/f").getJsonPointer()).isEqualTo("/a~1b~1c/d~1e~1f");
        Map<String, JsonNode> nested2 = nested1.get("d/e/f").asObject();
        assertThat(nested2.get("g/h").getJsonPointer()).isEqualTo("/a~1b~1c/d~1e~1f/g~1h");
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
                .asObject().get("/a/")
                .asArray().get(2)
                .asArray().get(0)
                .asObject().get("/b/c~")
                .asObject().get("~/")
                .asArray().get(0)
                .asObject().get("nested");

        assertThat(nestedNode.isBoolean()).isTrue();
        assertThat(nestedNode.getJsonPointer()).isEqualTo("/0/~1a~1/2/0/~1b~1c~0/~0~1/0/nested");
    }

    @ParameterizedTest
    @MethodSource("getScenarios")
    void shouldEscapeWhenRegisteringSchemas(String schema, String errorLocation) {
        Validator validator = new ValidatorFactory().createValidator();
        URI uri = validator.registerSchema(schema);

        Validator.Result result = validator.validate(uri, "{}");

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/$ref",
                errorLocation,
                "",
                null,
                "False schema always fails"
        );
    }

    static Stream<Arguments> getScenarios() {
        return Stream.of(
                Arguments.of(
                        """
                                {
                                  "$id": "urn:test",
                                  "$ref": "#/$defs/x/y",
                                  "$defs": {
                                    "x/y": true,
                                    "x": {
                                      "y": false
                                    }
                                  }
                                }""",
                        "urn:test#/$defs/x/y"),
                Arguments.of(
                        """
                                {
                                  "$id": "urn:test",
                                  "$ref": "#/$defs/x~1y",
                                  "$defs": {
                                    "x/y": false,
                                    "x": {
                                      "y": true
                                    }
                                  }
                                }""",
                        "urn:test#/$defs/x~1y"),
                Arguments.of(
                        """
                                {
                                  "$id": "urn:test",
                                  "$ref": "#/~1",
                                  "/": false
                                  }
                                }""",
                        "urn:test#/~1")
        );
    }
}
