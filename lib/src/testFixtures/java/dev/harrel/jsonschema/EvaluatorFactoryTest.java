package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.harrel.jsonschema.Keyword.CONTENT_ENCODING;
import static dev.harrel.jsonschema.Keyword.CONTENT_MEDIA_TYPE;
import static dev.harrel.jsonschema.Keyword.DEFAULT;
import static dev.harrel.jsonschema.Keyword.DEPRECATED;
import static dev.harrel.jsonschema.Keyword.DESCRIPTION;
import static dev.harrel.jsonschema.Keyword.EXAMPLES;
import static dev.harrel.jsonschema.Keyword.READ_ONLY;
import static dev.harrel.jsonschema.Keyword.TITLE;
import static dev.harrel.jsonschema.Keyword.WRITE_ONLY;
import static dev.harrel.jsonschema.SimpleType.*;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

abstract class EvaluatorFactoryTest implements ProviderTest {
    private static final Map<SimpleType, String> TYPE_MAP = Map.of(
            NULL, "null",
            BOOLEAN, "true",
            STRING, "\"string\"",
            INTEGER, "1",
            NUMBER, "1.1",
            ARRAY, "[]",
            OBJECT, "{}"
    );
    final Dialect dialect;

    EvaluatorFactoryTest(SpecificationVersion version) {
        this.dialect = Dialects.OFFICIAL_DIALECTS.get(UriUtil.removeEmptyFragment(version.getId()));
    }

    @ParameterizedTest
    @MethodSource("getSupportedAnnotationKeywords")
    void shouldCreateEvaluatorOnlyForSupportedAnnotationTypes(String keyword, Set<SimpleType> supportedTypes) {
        testSupportedKeyword(keyword, supportedTypes);
    }

    SchemaParsingContext createCtx() {
        return createCtx(dialect.getDefaultVocabularyObject(), dialect.getDefaultVocabularyObject().keySet());
    }

    SchemaParsingContext createCtx(Map<String, Boolean> vocabularyObject, Set<String> activeVocabularies) {
        MetaSchemaData metaSchemaData = new MetaSchemaData(dialect, vocabularyObject, activeVocabularies);
        return new SchemaParsingContext(metaSchemaData, URI.create("urn:CoreEvaluatorFactoryTest"), emptyMap()) ;
    }

    void testSupportedKeyword(String keyword, Set<SimpleType> supportedTypes) {
        for (var entry : TYPE_MAP.entrySet()) {
            JsonNode wrappedNode = getJsonNodeFactory().create("{\"%s\": %s}".formatted(keyword, entry.getValue()));
            Optional<Evaluator> evaluator = dialect.getEvaluatorFactory().create(createCtx(), keyword, wrappedNode.asObject().get(keyword));
            if (supportedTypes.contains(entry.getKey())) {
                assertThat(evaluator)
                        .withFailMessage("Expected type [%s] to pass", entry.getKey())
                        .isPresent();
            } else {
                assertThat(evaluator)
                        .withFailMessage("Expected type [%s] to fail", entry.getKey())
                        .isEmpty();
            }
        }
    }

    void testUnsupportedKeyword(String keyword) {
        for (var entry : TYPE_MAP.entrySet()) {
            JsonNode wrappedNode = getJsonNodeFactory().create("{\"%s\": %s}".formatted(keyword, entry.getValue()));
            Optional<Evaluator> evaluator = dialect.getEvaluatorFactory().create(createCtx(), keyword, wrappedNode.asObject().get(keyword));
            if (entry.getKey() == STRING) {
                assertThat(evaluator).containsInstanceOf(AbstractEvaluatorFactory.AnnotationEvaluator.class);
            } else {
                assertThat(evaluator).isEmpty();
            }
        }
    }

    void testIgnoredKeyword(String keyword) {
        for (var entry : TYPE_MAP.entrySet()) {
            JsonNode wrappedNode = getJsonNodeFactory().create("{\"%s\": %s}".formatted(keyword, entry.getValue()));
            Optional<Evaluator> evaluator = dialect.getEvaluatorFactory().create(createCtx(), keyword, wrappedNode.asObject().get(keyword));
            assertThat(evaluator).isEmpty();
        }
    }

    private static Stream<Arguments> getSupportedAnnotationKeywords() {
        return Stream.of(
                Arguments.of(TITLE, Set.of(STRING)),
                Arguments.of(DESCRIPTION, Set.of(STRING)),
                Arguments.of(DEFAULT, Set.of(NULL, BOOLEAN, STRING, INTEGER, NUMBER, ARRAY, OBJECT)),
                Arguments.of(DEPRECATED, Set.of(BOOLEAN)),
                Arguments.of(EXAMPLES, Set.of(ARRAY)),
                Arguments.of(READ_ONLY, Set.of(BOOLEAN)),
                Arguments.of(WRITE_ONLY, Set.of(BOOLEAN)),

                // Not testing CONTENT_SCHEMA as this won't work as a single keyword (needs CONTENT_MEDIA_TYPE)
                Arguments.of(CONTENT_ENCODING, Set.of(STRING)),
                Arguments.of(CONTENT_MEDIA_TYPE, Set.of(STRING))
        );
    }
}