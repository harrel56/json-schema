package dev.harrel.jsonschema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static dev.harrel.jsonschema.util.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class EvaluationPathTest implements ProviderTest {

    private Validator validator;
    private URI uri;

    @BeforeEach
    void setUp() {
        validator = new ValidatorFactory().withJsonNodeFactory(getJsonNodeFactory()).createValidator();
        uri = URI.create("urn:test");
    }

    @Test
    void withProperties() {
        validator.registerSchema(uri, readResource("/evaluation-path/properties.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/properties.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/properties/foo/$ref/properties/bar/$ref/type",
                "urn:test#/$defs/reffed2",
                "/foo/bar",
                "type"
        );
    }

    @Test
    void withAnchor() {
        validator.registerSchema(uri, readResource("/evaluation-path/anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/properties/foo/$ref/properties/bar/$ref/type",
                "urn:test#/$defs/reffed2",
                "/foo/bar",
                "type"
        );
    }

    @Test
    void withDynamicAnchor() {
        validator.registerSchema(uri, readResource("/evaluation-path/dynamic-anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/dynamic-anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/properties/foo/$ref/properties/bar/$ref/type",
                "urn:test#/$defs/reffed2",
                "/foo/bar",
                "type"
        );
    }

    @Test
    void withId() {
        validator.registerSchema(uri, readResource("/evaluation-path/id.schema.json"));
        Validator.Result result = validator.validate(URI.create("urn:root"), readResource("/evaluation-path/id.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/properties/foo/$ref/properties/bar/$ref/type",
                "urn:root#/$defs/reffed2",
                "/foo/bar",
                "type"
        );
    }

    @Test
    void withRemote() {
        validator.registerSchema(uri, readResource("/evaluation-path/remote.schema.json"));
        validator.registerSchema(URI.create("urn:anchor"), readResource("/evaluation-path/anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/properties/foo/$ref/properties/bar/$ref/type",
                "urn:anchor#/$defs/reffed2",
                "/foo/bar",
                "type"
        );
    }

    @Test
    void withFlat() {
        validator.registerSchema(uri, readResource("/evaluation-path/flat.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/flat.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/properties/foo/$ref/properties/bar/$ref/properties/baz",
                "urn:test#/properties/baz",
                "/foo/bar/baz",
                null,
                "False schema always fails"
        );
    }

    @Test
    void withItems() {
        validator.registerSchema(uri, readResource("/evaluation-path/items.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/items.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/items/$ref/items/$ref",
                "urn:test#/custom/2/0/1",
                "/0/0",
                null,
                "False schema always fails"
        );
    }

    @Test
    void withEscapedJsonPointer() {
        String schema = """
                {
                  "$id": "urn:test",
                  "properties": {
                    "x/y": {
                      "required": ["a/b"],
                      "title": "/hmm"
                    }
                  }
                }""";
        String instance = """
                {
                  "x/y": {
                    "a/b": true
                  }
                }""";
        validator.registerSchema(uri, schema);
        Validator.Result result = validator.validate(uri, instance);

        assertThat(result.isValid()).isTrue();
        List<Annotation> annotations = result.getAnnotations();
        assertThat(annotations).hasSize(2);
        assertAnnotation(
                annotations.get(0),
                "/properties/x~1y/title",
                "urn:test#/properties/x~1y",
                "/x~1y",
                "title",
                "/hmm"
        );
        assertAnnotation(
                annotations.get(1),
                "/properties",
                "urn:test#",
                "",
                "properties",
                Set.of("x/y")
        );
    }

    @Test
    void withValidPropertyNames() {
        String schema = """
                {
                  "$id": "urn:test",
                  "propertyNames": {
                    "maxLength": 2
                  }
                }""";
        String instance = """
                {
                  "/1": true
                }""";
        validator.registerSchema(uri, schema);
        Validator.Result result = validator.validate(uri, instance);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void withValidPropertyNamesWithConst() {
        String schema = """
                {
                  "$id": "urn:test",
                  "propertyNames": {
                    "const": "foo"
                  }
                }""";
        String instance = """
                {
                  "foo": true
                }""";
        validator.registerSchema(uri, schema);
        Validator.Result result = validator.validate(uri, instance);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void withValidPropertyNamesWithEnum() {
        String schema = """
                {
                  "$id": "urn:test",
                  "propertyNames": {
                    "enum": ["a", "b", "c"]
                  }
                }""";
        String instance = """
                {
                  "a": true,
                  "c": false
                }""";
        validator.registerSchema(uri, schema);
        Validator.Result result = validator.validate(uri, instance);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void withInvalidPropertyNames() {
        String schema = """
                {
                  "$id": "urn:test",
                  "propertyNames": {
                    "maxLength": 2
                  }
                }""";
        String instance = """
                {
                  "///": true
                }""";
        validator.registerSchema(uri, schema);
        Validator.Result result = validator.validate(uri, instance);

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/propertyNames/maxLength",
                "urn:test#/propertyNames",
                "",
                "maxLength",
                "\"///\" is longer than 2 characters"
        );
    }

    @Test
    void withInvalidPropertyNamesWithConst() {
        String schema = """
                {
                  "$id": "urn:test",
                  "properties": {
                    "outer": {
                      "properties": {
                        "arr": {
                          "items": {
                            "propertyNames": {
                              "const": "foo"
                            }
                          }
                        }
                      }
                    }
                  }

                }""";
        String instance = """
                {
                  "outer": {
                    "arr": [{
                      "bar": true
                    }]
                  }
                }""";
        validator.registerSchema(uri, schema);
        Validator.Result result = validator.validate(uri, instance);

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/properties/outer/properties/arr/items/propertyNames/const",
                "urn:test#/properties/outer/properties/arr/items/propertyNames",
                "/outer/arr/0",
                "const",
                "Expected foo"
        );
    }
}