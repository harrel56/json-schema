package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

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
    void shouldBeTurnedOnWhenNoVocabulariesWithAdditionalFactory() {
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
    void shouldBeTurnedOnWhenNoVocabulariesWithDialect() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withDialect(new Dialects.Draft2020Dialect() {
                    @Override
                    public EvaluatorFactory getEvaluatorFactory() {
                        return new FormatEvaluatorFactory();
                    }
                })
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldBeTurnedOffWithFormatVocabulariesWithAdditionalFactory() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY))
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldBeTurnedOffWithFormatVocabulariesWithDialect() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withDialect(new Dialects.Draft2020Dialect() {
                    @Override
                    public EvaluatorFactory getEvaluatorFactory() {
                        return new FormatEvaluatorFactory(Vocabulary.FORMAT_ASSERTION_VOCABULARY);
                    }
                })
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldBeTurnedOnWithValidationVocabulariesWithAdditionalFactory() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory(Vocabulary.VALIDATION_VOCABULARY))
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isFalse();
    }

    // TODO add test with custom meta-schema which turns on this vocab so validation actually occurs
    @Test
    void shouldBeTurnedOnWithValidationVocabulariesWithDialect() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withDialect(new Dialects.Draft2020Dialect() {
                    @Override
                    public EvaluatorFactory getEvaluatorFactory() {
                        return new FormatEvaluatorFactory(Vocabulary.VALIDATION_VOCABULARY);
                    }
                })
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isFalse();
    }
}