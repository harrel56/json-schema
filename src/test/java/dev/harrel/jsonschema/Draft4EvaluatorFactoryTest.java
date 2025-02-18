package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static dev.harrel.jsonschema.Keyword.*;
import static dev.harrel.jsonschema.SimpleType.*;

public abstract class Draft4EvaluatorFactoryTest extends EvaluatorFactoryTest {
    public Draft4EvaluatorFactoryTest() {
        super(SpecificationVersion.DRAFT4);
    }

    @ParameterizedTest
    @MethodSource("getSupportedKeywords")
    void shouldCreateEvaluatorOnlyForSupportedTypes(String keyword, Set<SimpleType> supportedTypes) {
        testSupportedKeyword(keyword, supportedTypes);
    }

    @ParameterizedTest
    @MethodSource("getUnsupportedKeywords")
    void shouldCreateAnnotationEvaluatorForUnsupportedKeywords(String keyword) {
        testUnsupportedKeyword(keyword);
    }

    @ParameterizedTest
    @MethodSource("getIgnoredKeywords")
    void shouldNotCreateEvaluatorForIgnoredKeywords(String keyword) {
        testIgnoredKeyword(keyword);
    }

    private static Stream<Arguments> getSupportedKeywords() {
        return Stream.of(
                Arguments.of(DEPENDENCIES, Set.of(OBJECT)),

                Arguments.of(Keyword.TYPE, Set.of(STRING, ARRAY)),
                Arguments.of(Keyword.ENUM, Set.of(ARRAY)),
                Arguments.of(Keyword.MULTIPLE_OF, Set.of(INTEGER, NUMBER)),
                Arguments.of(Keyword.MAXIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(Keyword.MINIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(Keyword.MAX_LENGTH, Set.of(INTEGER)),
                Arguments.of(Keyword.MIN_LENGTH, Set.of(INTEGER)),
                Arguments.of(Keyword.PATTERN, Set.of(STRING)),
                Arguments.of(Keyword.MAX_ITEMS, Set.of(INTEGER)),
                Arguments.of(Keyword.MIN_ITEMS, Set.of(INTEGER)),
                Arguments.of(Keyword.UNIQUE_ITEMS, Set.of(BOOLEAN)),
                Arguments.of(Keyword.MAX_PROPERTIES, Set.of(INTEGER)),
                Arguments.of(Keyword.MIN_PROPERTIES, Set.of(INTEGER)),
                Arguments.of(Keyword.REQUIRED, Set.of(ARRAY)),

                Arguments.of(Keyword.ITEMS, Set.of(BOOLEAN, OBJECT, ARRAY)),
                Arguments.of(Keyword.ADDITIONAL_ITEMS, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.ADDITIONAL_PROPERTIES, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.PROPERTIES, Set.of(OBJECT)),
                Arguments.of(Keyword.PATTERN_PROPERTIES, Set.of(OBJECT)),
                Arguments.of(Keyword.ALL_OF, Set.of(ARRAY)),
                Arguments.of(Keyword.ANY_OF, Set.of(ARRAY)),
                Arguments.of(Keyword.ONE_OF, Set.of(ARRAY)),
                Arguments.of(Keyword.NOT, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(Keyword.REF, Set.of(STRING))
        );
    }

    private static Stream<String> getUnsupportedKeywords() {
        return Stream.of(ID, DYNAMIC_REF, RECURSIVE_REF, UNEVALUATED_ITEMS, UNEVALUATED_PROPERTIES, PREFIX_ITEMS,
                MAX_CONTAINS, MIN_CONTAINS, DEPENDENT_REQUIRED, DEPENDENT_SCHEMAS, IF, THEN, ELSE, CONST, PROPERTY_NAMES,
                CONTAINS, EXCLUSIVE_MAXIMUM, EXCLUSIVE_MINIMUM);
    }

    private static Stream<String> getIgnoredKeywords() {
        return Stream.of(LEGACY_ID, SCHEMA, DEFINITIONS);
    }
}