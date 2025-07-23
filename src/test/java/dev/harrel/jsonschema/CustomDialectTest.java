package dev.harrel.jsonschema;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class CustomDialectTest {
    @ParameterizedTest
    @MethodSource("cases")
    void takesActiveVocabsFromKeyword(String schemaKeyword) {
        CustomDialect dialect = new CustomDialect();
        Validator validator = new ValidatorFactory()
                .withDialect(dialect)
                .withDefaultDialect(dialect)
                .createValidator();
        String metaSchema = """
                {
                  %s
                  "$id": "https://harrel.dev/schema",
                  "$vocabulary": {
                    "urn:custom-vocab": true
                  },
                  "type": "object"
                }""".formatted(schemaKeyword);
        String schema = """
                {
                  "$schema": "https://harrel.dev/schema",
                  "$id": "urn:test",
                  "custom": "invalid"
                }""";
        validator.registerSchema(metaSchema);
        validator.registerSchema(schema);

        Validator.Result result = validator.validate(URI.create("urn:test"), schema);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertError(
                result.getErrors().getFirst(),
                "/custom",
                "urn:test#",
                "",
                "custom",
                "custom keyword failed"
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void takesActiveVocabsFromDialect(String schemaKeyword) {
        CustomDialect dialect = new CustomDialect() {
            @Override
            public Map<String, Boolean> getDefaultVocabularyObject() {
                return Map.of("urn:custom-vocab", true);
            }
        };
        Validator validator = new ValidatorFactory()
                .withDialect(dialect)
                .withDefaultDialect(dialect)
                .createValidator();
        String metaSchema = """
                {
                  %s
                  "$id": "https://harrel.dev/schema",
                  "type": "object"
                }""".formatted(schemaKeyword);
        String schema = """
                {
                  "$schema": "https://harrel.dev/schema",
                  "$id": "urn:test",
                  "custom": "invalid"
                }""";
        validator.registerSchema(metaSchema);
        validator.registerSchema(schema);

        Validator.Result result = validator.validate(URI.create("urn:test"), schema);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertError(
                result.getErrors().getFirst(),
                "/custom",
                "urn:test#",
                "",
                "custom",
                "custom keyword failed"
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void noActiveVocabsFromKeywordNorDialect(String schemaKeyword) {
        CustomDialect dialect = new CustomDialect();
        Validator validator = new ValidatorFactory()
                .withDialect(dialect)
                .withDefaultDialect(dialect)
                .createValidator();
        String metaSchema = """
                {
                  %s
                  "$id": "https://harrel.dev/schema",
                  "type": "object"
                }""".formatted(schemaKeyword);
        String schema = """
                {
                  "$schema": "https://harrel.dev/schema",
                  "$id": "urn:test",
                  "custom": "invalid"
                }""";
        validator.registerSchema(metaSchema);
        validator.registerSchema(schema);

        Validator.Result result = validator.validate(URI.create("urn:test"), schema);
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("cases")
    void emptyKeywordOverridesDialect(String schemaKeyword) {
        CustomDialect dialect = new CustomDialect() {
            @Override
            public Map<String, Boolean> getDefaultVocabularyObject() {
                return Map.of("urn:custom-vocab", true);
            }
        };
        Validator validator = new ValidatorFactory()
                .withDialect(dialect)
                .withDefaultDialect(dialect)
                .createValidator();
        String metaSchema = """
                {
                  %s
                  "$id": "https://harrel.dev/schema",
                  "$vocabulary": {},
                  "type": "object"
                }""".formatted(schemaKeyword);
        String schema = """
                {
                  "$schema": "https://harrel.dev/schema",
                  "$id": "urn:test",
                  "custom": "invalid"
                }""";
        validator.registerSchema(metaSchema);
        validator.registerSchema(schema);

        Validator.Result result = validator.validate(URI.create("urn:test"), schema);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldSkipSchemaValidationWhenDefaultDialectLacksMetaSchema() {
        var dialect = new Dialects.Draft2020Dialect() {
            @Override
            public String getMetaSchema() {
                return null;
            }
        };
        Validator validator = new ValidatorFactory().withDefaultDialect(dialect).createValidator();
        validator.registerSchema("""
                {
                  "$id": "urn:test",
                  "type": null
                }""");
        Validator.Result result = validator.validate(URI.create("urn:test"), "1");
        assertThat(result.isValid()).isTrue();
    }

    static Stream<Named<String>> cases() {
        return Stream.of(
                Named.of("basic", "\"$schema\": \"https://json-schema.org/draft/2020-12/schema\","),
                Named.of("recursive", "\"$schema\": \"https://harrel.dev/schema\","),
                Named.of("default", "")
        );
    }

    static class CustomEvaluatorFactory implements EvaluatorFactory {
        @Override
        public Optional<Evaluator> create(SchemaParsingContext spc, String fieldName, JsonNode fieldNode) {
            if ("custom".equals(fieldName) && spc.getMetaValidationData().activeVocabularies.contains("urn:custom-vocab")) {
                return Optional.of((ctx, node) -> {
                    if (node.isString() && node.asString().equals("custom")) {
                        return Evaluator.Result.success();
                    }
                    return Evaluator.Result.failure("custom keyword failed");
                });
            }
            return Optional.empty();
        }
    }

    static class CustomDialect implements Dialect {
        @Override
        public SpecificationVersion getSpecificationVersion() {
            return SpecificationVersion.DRAFT2020_12;
        }

        @Override
        public String getMetaSchema() {
            return "https://harrel.dev/schema";
        }

        @Override
        public EvaluatorFactory getEvaluatorFactory() {
            return new CustomEvaluatorFactory();
        }

        @Override
        public Set<String> getSupportedVocabularies() {
            return Set.of("urn:custom-vocab");
        }

        @Override
        public Set<String> getRequiredVocabularies() {
            return Set.of();
        }

        @Override
        public Map<String, Boolean> getDefaultVocabularyObject() {
            return Map.of();
        }
    }
}
