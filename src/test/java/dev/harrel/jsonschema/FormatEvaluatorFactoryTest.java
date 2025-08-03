package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormatEvaluatorFactoryTest {
    static final Set<String> FORMAT_ASSERTION_VOCABULARY = Set.of(Vocabulary.Draft2020.FORMAT_ASSERTION, Vocabulary.Draft2019.FORMAT);

    @Test
    void shouldCreateEvaluatorWhenVocabsOverlap() {
        JacksonNode.Factory nodeFactory = new JacksonNode.Factory();
        Set<String> vocabs = Set.of("a", "b", "c");
        FormatEvaluatorFactory factory = new FormatEvaluatorFactory(vocabs);

        SchemaParsingContext ctx = mock(SchemaParsingContext.class);
        when(ctx.getMetaSchemaData()).thenReturn(new MetaSchemaData(new Dialects.Draft2020Dialect(), Map.of(), Set.of("c")));
        Optional<Evaluator> evaluator = factory.create(ctx, "format", nodeFactory.create("\"date\""));
        assertThat(evaluator).isPresent();
    }

    @Test
    void shouldNotCreateEvaluatorWhenVocabsDontOverlap() {
        JacksonNode.Factory nodeFactory = new JacksonNode.Factory();
        Set<String> vocabs = Set.of("a", "b", "c");
        FormatEvaluatorFactory factory = new FormatEvaluatorFactory(vocabs);

        SchemaParsingContext ctx = mock(SchemaParsingContext.class);
        when(ctx.getMetaSchemaData()).thenReturn(new MetaSchemaData(new Dialects.Draft2020Dialect(), Map.of(), Set.of("d")));
        Optional<Evaluator> evaluator = factory.create(ctx, "format", nodeFactory.create("\"date\""));
        assertThat(evaluator).isEmpty();
    }

    @Test
    void shouldMakeDefensiveCopyOfVocabularies() {
        JacksonNode.Factory nodeFactory = new JacksonNode.Factory();
        Set<String> vocabs = new HashSet<>(Set.of("a", "b", "c"));
        FormatEvaluatorFactory factory = new FormatEvaluatorFactory(vocabs);
        vocabs.remove("c");

        SchemaParsingContext ctx = mock(SchemaParsingContext.class);
        when(ctx.getMetaSchemaData()).thenReturn(new MetaSchemaData(new Dialects.Draft2020Dialect(), Map.of(), Set.of("c")));
        Optional<Evaluator> evaluator = factory.create(ctx, "format", nodeFactory.create("\"date\""));
        assertThat(evaluator).isPresent();
    }

    @Test
    void shouldBeTurnedOnWhenNoVocabularies() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldBeTurnedOffWithFormatVocabularies() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(FORMAT_ASSERTION_VOCABULARY))
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldBeTurnedOnWithValidationVocabularies() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Set.of(Vocabulary.Draft2020.VALIDATION, Vocabulary.Draft2019.VALIDATION)))
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldBeTurnedOnWithFormatVocabulariesWithCustomVocabs() {
        String metaSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:meta-schema",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true,
                    "https://json-schema.org/draft/2020-12/vocab/format-assertion": false
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta-schema",
                  "format": "uri-reference"
                }""";
        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(metaSchema);
        URI schemaUri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(schemaUri, "\" \"");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldFailWithFormatVocabulariesWithMandatoryUnsupportedCustomVocabs() {
        String metaSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:meta-schema",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true,
                    "https://json-schema.org/draft/2020-12/vocab/format-assertion": true
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta-schema",
                  "format": "uri-reference"
                }""";
        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(metaSchema);
        assertThatThrownBy(() -> validator.registerSchema(schema))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Following vocabularies [https://json-schema.org/draft/2020-12/vocab/format-assertion] are required but not supported");
    }

    @Test
    void shouldBeTurnedOnWithFormatVocabulariesWithMandatorySupportedCustomVocabs() {
        String metaSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "urn:meta-schema",
                  "$vocabulary": {
                    "https://json-schema.org/draft/2020-12/vocab/core": true,
                    "https://json-schema.org/draft/2020-12/vocab/format-assertion": false
                  }
                }""";
        String schema = """
                {
                  "$schema": "urn:meta-schema",
                  "format": "uri-reference"
                }""";
        Dialect dialect = new Dialects.Draft2020Dialect() {
            @Override
            public Set<String> getSupportedVocabularies() {
                Set<String> vocabs = new HashSet<>(super.getSupportedVocabularies());
                vocabs.add(Vocabulary.Draft2020.FORMAT_ASSERTION);
                return vocabs;
            }
        };
        Validator validator = new ValidatorFactory()
                .withDialect(dialect) // this actually overrides official dialect - might be changed in the future?
                .withEvaluatorFactory(new FormatEvaluatorFactory(FORMAT_ASSERTION_VOCABULARY))
                .createValidator();
        validator.registerSchema(metaSchema);
        URI schemaUri = validator.registerSchema(schema);
        Validator.Result result = validator.validate(schemaUri, "\" \"");

        assertThat(result.isValid()).isFalse();
    }
}