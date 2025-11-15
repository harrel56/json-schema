package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Draft4IdTest {
    private static final URI DRAFT2020_SCHEMA = URI.create("urn:draft2020");
    private static final URI DRAFT4_SCHEMA = URI.create("urn:draft4");
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
        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");

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
        assertThatThrownBy(() -> validator.validate(DRAFT4_SCHEMA, "{}"))
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
        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");

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
        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");

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
        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");

        assertThat(res.isValid()).isTrue();
    }

    @Test
    void shouldFailForPreloadedRecursiveDraft4MetaSchema() {
        Validator validator = new ValidatorFactory().createValidator();
        assertThatThrownBy(() -> validator.registerSchema(RECURSIVE_DRAFT_4_META_SCHEMA))
                .isInstanceOf(MetaSchemaResolvingException.class)
                .hasMessage("Parsing meta-schema [urn:recursive-draft4] failed - only dialects explicitly added to a validator can be recursive");
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

    @Test
    void shouldRegisterEmbeddedSchemaWithDefaultDraft4() {
        String schema = """
                {
                  "definitions": {
                    "embedded": {
                      "id": "urn:draft4"
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft4Dialect())
                .createValidator();
        validator.registerSchema(schema);
        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");

        assertThat(res.isValid()).isTrue();
    }

    @Test
    void shouldNotRegisterEmbeddedNewIdWithDefaultDraft4() {
        String schema = """
                {
                  "definitions": {
                    "embedded": {
                      "$id": "urn:draft4"
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft4Dialect())
                .createValidator();
        validator.registerSchema(schema);
        assertThatThrownBy(() -> validator.validate(DRAFT4_SCHEMA, "{}"))
                .isInstanceOf(SchemaNotFoundException.class);
    }

    @Test
    void shouldRegisterEmbeddedSchemaWithExplicitDraft4() {
        String schema = """
                {
                  "$id": "urn:draft2020",
                  "definitions": {
                    "embedded": {
                      "$schema": "http://json-schema.org/draft-04/schema#",
                      "id": "urn:draft4",
                      "const": null
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        validator.registerSchema(schema);

        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");
        assertThat(res.isValid()).isTrue();
        res = validator.validate(DRAFT2020_SCHEMA, "{}");
        assertThat(res.isValid()).isTrue();
    }

    @Test
    void shouldNotRegisterEmbeddedNewIdWithExplicitDraft4() {
        String schema = """
                {
                  "id": "urn:draft2020",
                  "definitions": {
                    "embedded": {
                      "$schema": "http://json-schema.org/draft-04/schema#",
                      "$id": "urn:draft4"
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        validator.registerSchema(schema);
        assertThatThrownBy(() -> validator.validate(DRAFT4_SCHEMA, "{}"))
                .isInstanceOf(SchemaNotFoundException.class);
        assertThatThrownBy(() -> validator.validate(DRAFT2020_SCHEMA, "{}"))
                .isInstanceOf(SchemaNotFoundException.class);
    }

    /**
     * This is edge case when it doesn't work perfectly:
     * If embedded $schema is not recognized as registered dialect
     * then we fall back to a default dialect, so in most cases to $id usage
     */
    @Test
    void shouldNotRegisterEmbeddedSchemaWithDerivedDraft4MetaSchema() {
        String schema = """
                {
                  "id": "urn:draft2020",
                  "definitions": {
                    "embedded": {
                      "$schema": "urn:derived-draft4",
                      "id": "urn:draft4"
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        validator.registerSchema(DERIVED_DRAFT_4_META_SCHEMA);
        validator.registerSchema(schema);
        assertThatThrownBy(() -> validator.validate(DRAFT4_SCHEMA, "{}"))
                .isInstanceOf(SchemaNotFoundException.class);
        assertThatThrownBy(() -> validator.validate(DRAFT2020_SCHEMA, "{}"))
                .isInstanceOf(SchemaNotFoundException.class);
    }

    /**
     * This is edge case when it doesn't work perfectly:
     * If embedded $schema is not recognized as registered dialect
     * then we fall back to a default dialect, so in most cases to $id usage
     */
    @Test
    void shouldRegisterEmbeddedNewIdWithDerivedDraft4MetaSchema() {
        String schema = """
                {
                  "$id": "urn:draft2020",
                  "definitions": {
                    "embedded": {
                      "$schema": "urn:derived-draft4",
                      "$id": "urn:draft4",
                      "const": null
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory().createValidator();
        validator.registerSchema(DERIVED_DRAFT_4_META_SCHEMA);
        validator.registerSchema(schema);

        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");
        assertThat(res.isValid()).isTrue();
        res = validator.validate(DRAFT2020_SCHEMA, "{}");
        assertThat(res.isValid()).isTrue();
    }

    @Test
    void shouldRegisterEmbeddedSchemaWithExplicitDraft2020() {
        String schema = """
                {
                  "id": "urn:draft4",
                  "definitions": {
                    "embedded": {
                      "$schema": "https://json-schema.org/draft/2020-12/schema",
                      "$id": "urn:draft2020",
                      "dependencies": {
                        "fail": {
                          "not": {}
                        }
                      }
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft4Dialect())
                .createValidator();
        validator.registerSchema(schema);

        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");
        assertThat(res.isValid()).isTrue();
        res = validator.validate(DRAFT2020_SCHEMA, "{\"fail\": {}}");
        assertThat(res.isValid()).isTrue();
    }

    /**
     * Again, $id/id mismatch
     */
    @Test
    void shouldRegisterEmbeddedSchemaWithDerivedDraft2020MetaSchema() {
        String metaSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:derived-draft2020"
                }""";
        String schema = """
                {
                  "id": "urn:draft4",
                  "definitions": {
                    "embedded": {
                      "$schema": "urn:derived-draft2020",
                      "id": "urn:draft2020",
                      "dependencies": {
                        "fail": {
                          "not": {}
                        }
                      }
                    }
                  }
                }""";

        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new Dialects.Draft4Dialect())
                .createValidator();
        validator.registerSchema(metaSchema);
        validator.registerSchema(schema);

        Validator.Result res = validator.validate(DRAFT4_SCHEMA, "{}");
        assertThat(res.isValid()).isTrue();
        res = validator.validate(DRAFT2020_SCHEMA, "{\"fail\": {}}");
        assertThat(res.isValid()).isTrue();
    }
}
