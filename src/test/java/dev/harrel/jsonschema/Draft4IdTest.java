package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Draft4IdTest {
    private static final URI SCHEMA_URI = URI.create("urn:draft4");
    private static final String DERIVED_DRAFT_4_META_SCHEMA = """
            {
              "$schema": "http://json-schema.org/draft-04/schema#",
              "id": "urn:derived-draft4"
            }""";
    private static final String RECURSIVE_DRAFT_4_META_SCHEMA = """
            {
              "$schema": "urn:recursive-draft4",
              "id": "urn:recursive-draft4"
            }""";

    @Test
    void shouldRegisterRootIdWithDefaultDraft4() {
        String schema = """
                {
                  "id": "urn:draft4"
                }""";

        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft4Dialect())
                .createValidator();
        validator.registerSchema(schema);
        Validator.Result res = validator.validate(SCHEMA_URI, "{}");

        assertThat(res.isValid()).isTrue();
    }

    @Test
    void shouldNotRegisterNewRootIdWithDefaultDraft4() {
        String schema = """
                {
                  "$id": "urn:draft4"
                }""";

        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft4Dialect())
                .createValidator();
        validator.registerSchema(schema);
        assertThatThrownBy(() -> validator.validate(SCHEMA_URI, "{}"))
                .isInstanceOf(SchemaNotFoundException.class);
    }

    @Test
    void shouldRegisterRootIdWithExplicitDraft4() {
        String schema = """
                {
                  "$schema": "http://json-schema.org/draft-04/schema#",
                  "id": "urn:draft4"
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        validator.registerSchema(schema);
        Validator.Result res = validator.validate(SCHEMA_URI, "{}");

        assertThat(res.isValid()).isTrue();
    }

    @Test
    void shouldRegisterRootIdWithPreloadedDerivedDraft4MetaSchema() {
        String schema = """
                {
                  "$schema": "urn:derived-draft4",
                  "id": "urn:draft4"
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        validator.registerSchema(DERIVED_DRAFT_4_META_SCHEMA);
        validator.registerSchema(schema);
        Validator.Result res = validator.validate(SCHEMA_URI, "{}");

        assertThat(res.isValid()).isTrue();
    }

    @Test
    void shouldRegisterRootIdWithLazyDerivedDraft4MetaSchema() {
        String schema = """
                {
                  "$schema": "urn:derived-draft4",
                  "id": "urn:draft4"
                }""";

        Validator validator = new ValidatorFactory()
                .withSchemaResolver(uri -> {
                    if ("urn:derived-draft4".equals(uri)) {
                        return SchemaResolver.Result.fromString(DERIVED_DRAFT_4_META_SCHEMA);
                    }
                    return SchemaResolver.Result.empty();
                })
                .createValidator();
        validator.registerSchema(schema);
        Validator.Result res = validator.validate(SCHEMA_URI, "{}");

        assertThat(res.isValid()).isTrue();
    }

    @Test
    void shouldFailForPreloadedRecursiveDraft4MetaSchema() {
        String schema = """
                {
                  "$schema": "urn:recursive-draft4",
                  "id": "urn:draft4"
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        /* This probably should contain more detailed message but as an edge case I guess it's fine */
        assertThatThrownBy(() -> validator.registerSchema(RECURSIVE_DRAFT_4_META_SCHEMA))
                .isInstanceOf(MetaSchemaResolvingException.class)
                .hasMessage("Cannot resolve meta-schema [urn:recursive-draft4]");
    }

    @Test
    void shouldFailForLazyRecursiveDraft4MetaSchema() {
        String schema = """
                {
                  "$schema": "urn:recursive-draft4",
                  "id": "urn:draft4"
                }""";

        Validator validator = new ValidatorFactory()
                .withSchemaResolver(uri -> {
                    if ("urn:recursive-draft4".equals(uri)) {
                        return SchemaResolver.Result.fromString(RECURSIVE_DRAFT_4_META_SCHEMA);
                    }
                    return SchemaResolver.Result.empty();
                })
                .createValidator();
        /* Again unnecessarily wrapped exception, fine for now */
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(MetaSchemaResolvingException.class)
                .hasMessage("Parsing meta-schema [urn:recursive-draft4] failed")
                .cause()
                .isInstanceOf(MetaSchemaResolvingException.class)
                .hasMessage("Parsing meta-schema [urn:recursive-draft4] failed - only dialects explicitly added to a validator can be recursive");
    }
}
