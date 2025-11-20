package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class IdKeywordTest {
    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void allowsEmptyFragmentsInIdRootSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$id": "urn:test#"
                }""";

        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();

        // with enabled schema validation
        validator = createValidator(version, false);

        uri = validator.registerSchema(schema);
        result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("lenientVersions")
    void allowsAnchorFragmentsInIdRootSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$id": "urn:test#anchor"
                }""";

        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();

        // with enabled schema validation
        validator = createValidator(version, false);

        uri = validator.registerSchema(schema);
        result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("strictVersions")
    void disallowsStrictJsonPointerFragmentsInIdRootSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);

        String schema = """
                {
                  "$id": "urn:test#/$defs/x"
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:test#/$defs/x] cannot contain non-empty fragments");

        // with enabled schema validation
        Validator validator2 = createValidator(version, false);

        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator2.registerSchema(schema));
        assertThat(exception).hasMessage("Schema [urn:test] failed to validate against meta-schema [%s]".formatted(version.getId()));
        List<Error> errors = exception.getErrors();
        assertThat(errors).hasSize(2);
        assertThat(errors.getFirst().getError()).isEqualTo("\"urn:test#/$defs/x\" does not match regular expression ^[^#]*#?$");
    }

    @ParameterizedTest
    @MethodSource("lenientVersions")
    void disallowsLenientJsonPointerFragmentsInIdRootSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);

        String schema = """
                {
                  "%s": "urn:test#/$defs/x"
                }""".formatted(Keyword.getIdKeyword(version));
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:test#/$defs/x] cannot contain fragments starting with '/'");

        // with enabled schema validation
        Validator validator2 = createValidator(version, false);

        assertThatThrownBy(() -> validator2.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:test#/$defs/x] cannot contain fragments starting with '/'");
    }

    @ParameterizedTest
    @MethodSource("strictVersions")
    void disallowsAnchorFragmentsInIdRootSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);

        String schema = """
                {
                  "$id": "urn:test#anchor"
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:test#anchor] cannot contain non-empty fragments");

        // with enabled schema validation
        Validator validator2 = createValidator(version, false);

        InvalidSchemaException exception = catchThrowableOfType(InvalidSchemaException.class, () -> validator2.registerSchema(schema));
        assertThat(exception).hasMessage("Schema [urn:test] failed to validate against meta-schema [%s]".formatted(version.getId()));
        List<Error> errors = exception.getErrors();
        assertThat(errors).hasSize(2);
        assertThat(errors.getFirst().getError()).isEqualTo("\"urn:test#anchor\" does not match regular expression ^[^#]*#?$");
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void allowsEmptyFragmentsInIdSubSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "$id": "urn:sub#"
                    }
                  }
                }""";

        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();

        // with enabled schema validation
        validator = createValidator(version, false);

        uri = validator.registerSchema(schema);
        result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("strictVersions")
    void disallowsStrictJsonPointerFragmentsInIdSubSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "$id": "urn:sub#/$defs/x"
                    }
                  }
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:sub#/$defs/x] cannot contain non-empty fragments");

        // with enabled schema validation
        Validator validator2 = createValidator(version, false);
        URI uri = URI.create("urn:sub");

        assertThatThrownBy(() -> validator2.registerSchema(uri, schema))
                .isInstanceOf(InvalidSchemaException.class)
                .hasMessage("Schema [urn:sub] failed to validate against meta-schema [%s]".formatted(version.getId()));
    }

    @ParameterizedTest
    @MethodSource("lenientVersions")
    void disallowsLenientJsonPointerFragmentsInIdSubSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "%s": "urn:sub#/$defs/x"
                    }
                  }
                }""".formatted(Keyword.getIdKeyword(version));
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:sub#/$defs/x] cannot contain fragments starting with '/'");

        // with enabled schema validation
        Validator validator2 = createValidator(version, false);

        assertThatThrownBy(() -> validator2.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:sub#/$defs/x] cannot contain fragments starting with '/'");
    }

    @ParameterizedTest
    @MethodSource("lenientVersions")
    void allowsAnchorFragmentsInIdSubSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "$id": "urn:sub#anchor"
                    }
                  }
                }""";
        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();

        // with enabled schema validation
        validator = createValidator(version, false);

        uri = validator.registerSchema(schema);
        result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("strictVersions")
    void disallowsAnchorFragmentsInIdSubSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "$id": "urn:sub#anchor"
                    }
                  }
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [urn:sub#anchor] cannot contain non-empty fragments");

        // with enabled schema validation
        Validator validator2 = createValidator(version, false);
        URI uri = URI.create("urn:root");

        assertThatThrownBy(() -> validator2.registerSchema(uri, schema))
                .isInstanceOf(InvalidSchemaException.class)
                .hasMessage("Schema [urn:root] failed to validate against meta-schema [%s]".formatted(version.getId()));
    }

    @ParameterizedTest
    @MethodSource("lenientVersions")
    void allowsSoleAnchorFragmentsInIdSubSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "$id": "#anchor"
                    }
                  }
                }""";
        URI uri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();

        // with enabled schema validation
        validator = createValidator(version, false);

        uri = validator.registerSchema(schema);
        result = validator.validate(uri, "true");
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("strictVersions")
    void disallowsSoleAnchorFragmentsInIdSubSchema(SpecificationVersion version) {
        // with disabled schema validation
        Validator validator = createValidator(version, true);
        String schema = """
                {
                  "$defs": {
                    "x": {
                      "$id": "#anchor"
                    }
                  }
                }""";
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("$id [#anchor] cannot contain non-empty fragments");

        // with enabled schema validation
        Validator validator2 = createValidator(version, false);
        URI uri = URI.create("urn:root");

        assertThatThrownBy(() -> validator2.registerSchema(uri, schema))
                .isInstanceOf(InvalidSchemaException.class)
                .hasMessage("Schema [urn:root] failed to validate against meta-schema [%s]".formatted(version.getId()));
    }

    private Validator createValidator(SpecificationVersion version, boolean disabledSchemaValidation) {
        return new ValidatorFactory()
                .withDefaultDialect(Dialects.OFFICIAL_DIALECTS.get(UriUtil.removeEmptyFragment(version.getId())))
                .withDisabledSchemaValidation(disabledSchemaValidation)
                .createValidator();
    }

    static Stream<SpecificationVersion> strictVersions() {
        return Arrays.stream(SpecificationVersion.values()).filter(version -> version.getOrder() > SpecificationVersion.DRAFT7.getOrder());
    }

    static Stream<SpecificationVersion> lenientVersions() {
        return Arrays.stream(SpecificationVersion.values()).filter(version -> version.getOrder() <= SpecificationVersion.DRAFT7.getOrder());
    }
}
