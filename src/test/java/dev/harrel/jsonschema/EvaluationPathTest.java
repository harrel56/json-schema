package dev.harrel.jsonschema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static dev.harrel.jsonschema.TestUtil.readResource;
import static org.assertj.core.api.Assertions.assertThat;

class EvaluationPathTest {

    private Validator validator;
    private URI uri;

    @BeforeEach
    void setUp() {
        validator = new ValidatorFactory().withDefaultMetaSchemaUri(null).createValidator();
        uri = URI.create("urn:test");
    }

    @Test
    void withProperties() {
        validator.registerSchema(uri, readResource("/evaluation-path/properties.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/properties.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<EvaluationItem> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).evaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).schemaLocation()).isEqualTo("urn:test#/$defs/reffed2");
        assertThat(errors.get(0).instanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).keyword()).isEqualTo("type");
        assertThat(errors.get(0).valid()).isFalse();
        assertThat(errors.get(0).annotation()).isNull();
        assertThat(errors.get(0).error()).isNotNull();
    }

    @Test
    void withAnchor() {
        validator.registerSchema(uri, readResource("/evaluation-path/anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<EvaluationItem> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).evaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).schemaLocation()).isEqualTo("urn:test#/$defs/reffed2");
        assertThat(errors.get(0).instanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).keyword()).isEqualTo("type");
        assertThat(errors.get(0).valid()).isFalse();
        assertThat(errors.get(0).annotation()).isNull();
        assertThat(errors.get(0).error()).isNotNull();
    }

    @Test
    void withDynamicAnchor() {
        validator.registerSchema(uri, readResource("/evaluation-path/dynamic-anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/dynamic-anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<EvaluationItem> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).evaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).schemaLocation()).isEqualTo("urn:test#/$defs/reffed2");
        assertThat(errors.get(0).instanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).keyword()).isEqualTo("type");
        assertThat(errors.get(0).valid()).isFalse();
        assertThat(errors.get(0).annotation()).isNull();
        assertThat(errors.get(0).error()).isNotNull();
    }

    @Test
    void withId() {
        validator.registerSchema(uri, readResource("/evaluation-path/id.schema.json"));
        Validator.Result result = validator.validate(URI.create("root"), readResource("/evaluation-path/id.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<EvaluationItem> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).evaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).schemaLocation()).isEqualTo("urn:test#/$defs/reffed2");
        assertThat(errors.get(0).instanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).keyword()).isEqualTo("type");
        assertThat(errors.get(0).valid()).isFalse();
        assertThat(errors.get(0).annotation()).isNull();
        assertThat(errors.get(0).error()).isNotNull();
    }

    @Test
    void withRemote() {
        validator.registerSchema(uri, readResource("/evaluation-path/remote.schema.json"));
        validator.registerSchema(URI.create("urn:anchor"), readResource("/evaluation-path/anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<EvaluationItem> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).evaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).schemaLocation()).isEqualTo("urn:anchor#/$defs/reffed2");
        assertThat(errors.get(0).instanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).keyword()).isEqualTo("type");
        assertThat(errors.get(0).valid()).isFalse();
        assertThat(errors.get(0).annotation()).isNull();
        assertThat(errors.get(0).error()).isNotNull();
    }

    @Test
    void withFlat() {
        validator.registerSchema(uri, readResource("/evaluation-path/flat.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/flat.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<EvaluationItem> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).evaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/properties/baz");
        assertThat(errors.get(0).schemaLocation()).isEqualTo("urn:test#/properties/baz");
        assertThat(errors.get(0).instanceLocation()).isEqualTo("/foo/bar/baz");
        assertThat(errors.get(0).keyword()).isNull();
        assertThat(errors.get(0).valid()).isFalse();
        assertThat(errors.get(0).annotation()).isNull();
        assertThat(errors.get(0).error()).isEqualTo("False schema always fails.");
    }

    @Test
    void withItems() {
        validator.registerSchema(uri, readResource("/evaluation-path/items.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/items.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<EvaluationItem> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).evaluationPath()).isEqualTo("/items/$ref/items/$ref");
        assertThat(errors.get(0).schemaLocation()).isEqualTo("urn:test#/custom/2/0/1");
        assertThat(errors.get(0).instanceLocation()).isEqualTo("/0/0");
        assertThat(errors.get(0).keyword()).isNull();
        assertThat(errors.get(0).valid()).isFalse();
        assertThat(errors.get(0).annotation()).isNull();
        assertThat(errors.get(0).error()).isEqualTo("False schema always fails.");
    }
}