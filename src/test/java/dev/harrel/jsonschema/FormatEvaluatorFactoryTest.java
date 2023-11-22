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
    void shouldRespectVocabulariesSemantics() {
        String schema = """
                {
                  "format": "uri-reference"
                }""";
        Validator.Result result = new ValidatorFactory()
                .withDialect(new FormatDialect())
                .validate(schema, "\" \"");

        assertThat(result.isValid()).isFalse();
    }

    private static class FormatDialect extends Dialects.Draft2020Dialect {
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