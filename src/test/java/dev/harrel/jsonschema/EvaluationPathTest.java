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
        validator = new ValidatorFactory().withDisabledSchemaValidation(true).createValidator();
        uri = URI.create("urn:test");
    }

    @Test
    void withProperties() {
        validator.registerSchema(uri, readResource("/evaluation-path/properties.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/properties.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).getSchemaLocation()).isEqualTo("urn:test#/$defs/reffed2");
        assertThat(errors.get(0).getInstanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).getKeyword()).isEqualTo("type");
        assertThat(errors.get(0).getError()).isNotNull();
    }

    @Test
    void withAnchor() {
        validator.registerSchema(uri, readResource("/evaluation-path/anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).getSchemaLocation()).isEqualTo("urn:test#/$defs/reffed2");
        assertThat(errors.get(0).getInstanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).getKeyword()).isEqualTo("type");
        assertThat(errors.get(0).getError()).isNotNull();
    }

    @Test
    void withDynamicAnchor() {
        validator.registerSchema(uri, readResource("/evaluation-path/dynamic-anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/dynamic-anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).getSchemaLocation()).isEqualTo("urn:test#/$defs/reffed2");
        assertThat(errors.get(0).getInstanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).getKeyword()).isEqualTo("type");
        assertThat(errors.get(0).getError()).isNotNull();
    }

    @Test
    void withId() {
        validator.registerSchema(uri, readResource("/evaluation-path/id.schema.json"));
        Validator.Result result = validator.validate(URI.create("root"), readResource("/evaluation-path/id.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).getSchemaLocation()).isEqualTo("urn:test#/$defs/reffed2");
        assertThat(errors.get(0).getInstanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).getKeyword()).isEqualTo("type");
        assertThat(errors.get(0).getError()).isNotNull();
    }

    @Test
    void withRemote() {
        validator.registerSchema(uri, readResource("/evaluation-path/remote.schema.json"));
        validator.registerSchema(URI.create("urn:anchor"), readResource("/evaluation-path/anchor.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/anchor.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/type");
        assertThat(errors.get(0).getSchemaLocation()).isEqualTo("urn:anchor#/$defs/reffed2");
        assertThat(errors.get(0).getInstanceLocation()).isEqualTo("/foo/bar");
        assertThat(errors.get(0).getKeyword()).isEqualTo("type");
        assertThat(errors.get(0).getError()).isNotNull();
    }

    @Test
    void withFlat() {
        validator.registerSchema(uri, readResource("/evaluation-path/flat.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/flat.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/properties/foo/$ref/properties/bar/$ref/properties/baz");
        assertThat(errors.get(0).getSchemaLocation()).isEqualTo("urn:test#/properties/baz");
        assertThat(errors.get(0).getInstanceLocation()).isEqualTo("/foo/bar/baz");
        assertThat(errors.get(0).getKeyword()).isNull();
        assertThat(errors.get(0).getError()).isEqualTo("False schema always fails.");
    }

    @Test
    void withItems() {
        validator.registerSchema(uri, readResource("/evaluation-path/items.schema.json"));
        Validator.Result result = validator.validate(uri, readResource("/evaluation-path/items.instance.json"));

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEvaluationPath()).isEqualTo("/items/$ref/items/$ref");
        assertThat(errors.get(0).getSchemaLocation()).isEqualTo("urn:test#/custom/2/0/1");
        assertThat(errors.get(0).getInstanceLocation()).isEqualTo("/0/0");
        assertThat(errors.get(0).getKeyword()).isNull();
        assertThat(errors.get(0).getError()).isEqualTo("False schema always fails.");
    }
}