package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.harrel.jsonschema.SimpleType.*;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class Draft2019EvaluatorFactoryTest implements ProviderTest {
    private static final Map<SimpleType, String> TYPE_MAP = Map.of(
            NULL, "null",
            BOOLEAN, "true",
            STRING, "\"string\"",
            INTEGER, "1",
            NUMBER, "1.1",
            ARRAY, "[]",
            OBJECT, "{}"
    );

    @ParameterizedTest
    @MethodSource("getKeywords")
    void shouldCreateEvaluatorOnlyForSupportedTypes(String keyword, Set<SimpleType> supportedTypes) {
        Dialect dialect = new Dialects.Draft2019Dialect();
        EvaluatorFactory evaluatorFactory = dialect.getEvaluatorFactory();
        SchemaParsingContext ctx = new SchemaParsingContext(dialect, new SchemaRegistry(), URI.create("urn:CoreEvaluatorFactoryTest"), emptyMap());

        for (var entry : TYPE_MAP.entrySet()) {
            JsonNode wrappedNode = getJsonNodeFactory().create("{\"%s\": %s}".formatted(keyword, entry.getValue()));
            Optional<Evaluator> evaluator = evaluatorFactory.create(ctx, keyword, wrappedNode.asObject().get(keyword));
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

    private static Stream<Arguments> getKeywords() {
        return Stream.of(
                Arguments.of(Keyword.TYPE, Set.of(STRING, ARRAY)),
                Arguments.of(Keyword.CONST, Set.of(NULL, BOOLEAN, STRING, INTEGER, NUMBER, ARRAY, OBJECT)),
                Arguments.of(Keyword.ENUM, Set.of(ARRAY)),
                Arguments.of(Keyword.MULTIPLE_OF, Set.of(INTEGER, NUMBER)),
                Arguments.of(Keyword.MAXIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(Keyword.EXCLUSIVE_MAXIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(Keyword.MINIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(Keyword.EXCLUSIVE_MINIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(Keyword.MAX_LENGTH, Set.of(INTEGER)),
                Arguments.of(Keyword.MIN_LENGTH, Set.of(INTEGER)),
                Arguments.of(Keyword.PATTERN, Set.of(STRING)),
                Arguments.of(Keyword.MAX_ITEMS, Set.of(INTEGER)),
                Arguments.of(Keyword.MIN_ITEMS, Set.of(INTEGER)),
                Arguments.of(Keyword.UNIQUE_ITEMS, Set.of(BOOLEAN)),
                Arguments.of(Keyword.MAX_CONTAINS, Set.of(INTEGER)),
                Arguments.of(Keyword.MIN_CONTAINS, Set.of(INTEGER)),
                Arguments.of(Keyword.MAX_PROPERTIES, Set.of(INTEGER)),
                Arguments.of(Keyword.MIN_PROPERTIES, Set.of(INTEGER)),
                Arguments.of(Keyword.REQUIRED, Set.of(ARRAY)),
                Arguments.of(Keyword.DEPENDENT_REQUIRED, Set.of(OBJECT)),

                Arguments.of(Keyword.PREFIX_ITEMS, Set.of(ARRAY)),
                Arguments.of(Keyword.ITEMS, Set.of(BOOLEAN, OBJECT, ARRAY)),
                Arguments.of(Keyword.ADDITIONAL_ITEMS, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.CONTAINS, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.ADDITIONAL_PROPERTIES, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.PROPERTIES, Set.of(OBJECT)),
                Arguments.of(Keyword.PATTERN_PROPERTIES, Set.of(OBJECT)),
                Arguments.of(Keyword.DEPENDENT_SCHEMAS, Set.of(OBJECT)),
                Arguments.of(Keyword.PROPERTY_NAMES, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.IF, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.THEN, Set.of()),
                Arguments.of(Keyword.ELSE, Set.of()),
                Arguments.of(Keyword.ALL_OF, Set.of(ARRAY)),
                Arguments.of(Keyword.ANY_OF, Set.of(ARRAY)),
                Arguments.of(Keyword.ONE_OF, Set.of(ARRAY)),
                Arguments.of(Keyword.NOT, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.UNEVALUATED_ITEMS, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.UNEVALUATED_PROPERTIES, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.REF, Set.of(STRING)),
                Arguments.of(Keyword.DYNAMIC_REF, Set.of(STRING)),
                Arguments.of(Keyword.RECURSIVE_REF, Set.of(STRING))
        );
    }
}