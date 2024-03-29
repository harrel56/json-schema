package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FormatEvaluatorFactoryTest {

    @Test
    void shouldUseProvidedVocabularies() {
        JacksonNode.Factory nodeFactory = new JacksonNode.Factory();
        Set<String> vocabs = Set.of("a", "b", "c");
        FormatEvaluatorFactory factory = new FormatEvaluatorFactory(vocabs);

        Optional<Evaluator> evaluator = factory.create(mock(SchemaParsingContext.class), "format", nodeFactory.create("\"date\""));
        assertThat(evaluator).isPresent();
        assertThat(evaluator.get().getVocabularies()).isEqualTo(vocabs);
    }

    @Test
    void shouldMakeDefensiveCopyOfVocabularies() {
        JacksonNode.Factory nodeFactory = new JacksonNode.Factory();
        Set<String> vocabs = new HashSet<>(Set.of("a", "b", "c"));
        FormatEvaluatorFactory factory = new FormatEvaluatorFactory(vocabs);
        vocabs.add("d");

        Optional<Evaluator> evaluator = factory.create(mock(SchemaParsingContext.class), "format", nodeFactory.create("\"date\""));
        assertThat(evaluator).isPresent();
        assertThat(evaluator.get().getVocabularies()).isNotEqualTo(vocabs);
        assertThat(evaluator.get().getVocabularies()).containsExactly("a", "b", "c");
    }

    @Test
    void shouldUseDefaultVocabulariesWhenNoMetaSchema() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withDialect(new FormatDialect())
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldRespectVocabulariesFromMetaSchemaFirst() {
        String metaSchema = """
                {
                    "$id": "https://json-schema.org/draft/2020-12/schema",
                    "$vocabulary": {
                        "https://json-schema.org/draft/2020-12/vocab/core": true,
                        "https://json-schema.org/draft/2020-12/vocab/applicator": true,
                        "https://json-schema.org/draft/2020-12/vocab/unevaluated": true,
                        "https://json-schema.org/draft/2020-12/vocab/validation": true,
                        "https://json-schema.org/draft/2020-12/vocab/meta-data": true,
                        "https://json-schema.org/draft/2020-12/vocab/format-annotation": true,
                        "https://json-schema.org/draft/2020-12/vocab/content": true
                    }
                }""";
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "format": "uri-reference"
                }""";
        SchemaResolver resolver = uri -> {
            if (uri.equals(SpecificationVersion.DRAFT2020_12.getId())) {
                return SchemaResolver.Result.fromString(metaSchema);
            }
            return SchemaResolver.Result.empty();
        };
        Validator.Result result = new ValidatorFactory()
                .withDialect(new FormatDialect())
                .withSchemaResolver(resolver)
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldRespectVocabulariesFromCustomMetaSchema() {
        String metaSchema = """
                {
                    "$id": "https://json-schema.org/draft/2020-12/schema",
                    "$vocabulary": {
                        "https://json-schema.org/draft/2020-12/vocab/core": true,
                        "https://json-schema.org/draft/2020-12/vocab/applicator": true,
                        "https://json-schema.org/draft/2020-12/vocab/unevaluated": true,
                        "https://json-schema.org/draft/2020-12/vocab/validation": true,
                        "https://json-schema.org/draft/2020-12/vocab/meta-data": true,
                        "https://json-schema.org/draft/2020-12/vocab/format-assertion": true,
                        "https://json-schema.org/draft/2020-12/vocab/content": true
                    }
                }""";
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "format": "uri-reference"
                }""";
        SchemaResolver resolver = uri -> {
            if (uri.equals(SpecificationVersion.DRAFT2020_12.getId())) {
                return SchemaResolver.Result.fromString(metaSchema);
            }
            return SchemaResolver.Result.empty();
        };
        Validator.Result result = new ValidatorFactory()
                .withDialect(new FormatDialect())
                .withSchemaResolver(resolver)
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isFalse();
    }

    private static class FormatDialect extends Dialects.Draft2020Dialect {
        @Override
        public Optional<URI> getMetaSchemaUri() {
            return Optional.empty();
        }

        @Override
        public EvaluatorFactory getEvaluatorFactory() {
            return new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY);
        }

        @Override
        public Set<String> getSupportedVocabularies() {
            HashSet<String> vocabs = new HashSet<>(super.getSupportedVocabularies());
            vocabs.add(Vocabulary.Draft2020.FORMAT_ASSERTION);
            return vocabs;
        }

        @Override
        public Map<String, Boolean> getDefaultVocabularyObject() {
            HashMap<String, Boolean> obj = new HashMap<>(super.getDefaultVocabularyObject());
            obj.put(Vocabulary.Draft2020.FORMAT_ASSERTION, true);
            return obj;
        }
    }
}