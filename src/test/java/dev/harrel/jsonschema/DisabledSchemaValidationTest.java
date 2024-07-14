package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class DisabledSchemaValidationTest implements ProviderTest {
    @Test
    void shouldNotResolveMetaSchemas() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDisabledSchemaValidation(true)
                .createValidator();

        String schema = """
                {
                  "$schema": "urn:meta",
                  "type": "null"
                }""";

        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/type",
                "urn:schema#",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }

    @Test
    void shouldIgnoreTurnedOffVocabulariesBecauseMetaSchemaIsNotResolved() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDisabledSchemaValidation(true)
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta",
                  "type": "null"
                }""";

        validator.registerSchema(metaSchema);
        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/type",
                "urn:schema#",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }

    @Test
    void shouldIgnoreOptionalUnsupportedVocabulariesBecauseMetaSchemaIsNotResolved() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDisabledSchemaValidation(true)
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true,
                    "urn:unknown": false
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";

        validator.registerSchema(metaSchema);
        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldNotFailForUnsupportedRequiredVocabulariesBecauseMetaSchemaIsNotResolved() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDisabledSchemaValidation(true)
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true,
                    "urn:unknown": true
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";

        validator.registerSchema(metaSchema);
        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldNotFailForMissingRequiredVocabulariesBecauseMetaSchemaIsNotResolved() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDisabledSchemaValidation(true)
                .createValidator();

        String metaSchema = """
                {
                  "$id": "urn:meta",
                  "$vocabulary": {}
                }""";
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";

        validator.registerSchema(metaSchema);
        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldSupportEvaluatorWithEmptyVocabularies() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withEvaluatorFactory(new CustomEvaluatorFactory(Set.of()))
                .withDisabledSchemaValidation(true)
                .createValidator();

        String schema = """
                {
                  "custom": "null"
                }""";

        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/custom",
                "urn:schema#",
                "",
                "custom",
                "custom message"
        );
    }

    @Test
    void shouldSupportEvaluatorWithUnknownVocabularies() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withEvaluatorFactory(new CustomEvaluatorFactory(Set.of("urn:vocab1", "urn:vocab2")))
                .withDisabledSchemaValidation(true)
                .createValidator();

        String schema = """
                {
                  "custom": "null"
                }""";

        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/custom",
                "urn:schema#",
                "",
                "custom",
                "custom message"
        );
    }

    @Test
    void shouldUseDefaultDialectWhenNoSchemaKeyword() {
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new CustomDialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDisabledSchemaValidation(true)
                .createValidator();

        String schema = """
                {
                  "custom": "null"
                }""";

        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/custom",
                "urn:schema#",
                "",
                "custom",
                "custom message"
        );
    }

    @Test
    void shouldUseDefaultDialectWhenUnknownSchema() {
        Validator validator = new ValidatorFactory()
                .withDefaultDialect(new CustomDialect())
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDisabledSchemaValidation(true)
                .createValidator();

        String schema = """
                {
                  "$schema": "http://json-schema.org/draft-03/schema",
                  "custom": "null"
                }""";

        URI schemaUri = URI.create("urn:schema");
        validator.registerSchema(schemaUri, schema);
        Validator.Result result = validator.validate(schemaUri, "{}");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/custom",
                "urn:schema#",
                "",
                "custom",
                "custom message"
        );
    }

    static class CustomEvaluator implements Evaluator {
        private final Set<String> vocabs;

        CustomEvaluator(Set<String> vocabs) {
            this.vocabs = vocabs;
        }

        @Override
        public Result evaluate(EvaluationContext ctx, JsonNode node) {
            return Evaluator.Result.failure("custom message");
        }

        @Override
        public Set<String> getVocabularies() {
            return vocabs;
        }
    }

    static class CustomEvaluatorFactory implements EvaluatorFactory {
        private final Set<String> vocabs;

        CustomEvaluatorFactory(Set<String> vocabs) {
            this.vocabs = vocabs;
        }

        @Override
        public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode) {
            if (fieldName.equals("custom")) {
                return Optional.of(new CustomEvaluator(vocabs));
            } else {
                return Optional.empty();
            }
        }
    }

    static class CustomDialect extends Dialects.Draft2020Dialect {
        @Override
        public String getMetaSchema() {
            return "urn:meta";
        }

        @Override
        public EvaluatorFactory getEvaluatorFactory() {
            return new CustomEvaluatorFactory(Set.of("urn:vocab1", "urn:vocab2"));
        }

        @Override
        public Set<String> getSupportedVocabularies() {
            Set<String> vocabs = new HashSet<>(super.getSupportedVocabularies());
            vocabs.add("urn:vocab1");
            return vocabs;
        }

        @Override
        public Map<String, Boolean> getDefaultVocabularyObject() {
            Map<String, Boolean> map = new HashMap<>(super.getDefaultVocabularyObject());
            map.put("urn:vocab1", true);
            return map;
        }
    }
}
