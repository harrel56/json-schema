package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static dev.harrel.jsonschema.Keyword.*;
import static dev.harrel.jsonschema.SimpleType.*;

public abstract class Draft2020EvaluatorFactoryTest extends EvaluatorFactoryTest {
    public Draft2020EvaluatorFactoryTest() {
        super(SpecificationVersion.DRAFT2020_12);
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
                Arguments.of(TYPE, Set.of(STRING, ARRAY)),
                Arguments.of(CONST, Set.of(NULL, BOOLEAN, STRING, INTEGER, NUMBER, ARRAY, OBJECT)),
                Arguments.of(ENUM, Set.of(ARRAY)),
                Arguments.of(MULTIPLE_OF, Set.of(INTEGER, NUMBER)),
                Arguments.of(MAXIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(EXCLUSIVE_MAXIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(MINIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(EXCLUSIVE_MINIMUM, Set.of(INTEGER, NUMBER)),
                Arguments.of(MAX_LENGTH, Set.of(INTEGER)),
                Arguments.of(MIN_LENGTH, Set.of(INTEGER)),
                Arguments.of(PATTERN, Set.of(STRING)),
                Arguments.of(MAX_ITEMS, Set.of(INTEGER)),
                Arguments.of(MIN_ITEMS, Set.of(INTEGER)),
                Arguments.of(UNIQUE_ITEMS, Set.of(BOOLEAN)),
                Arguments.of(MAX_CONTAINS, Set.of(INTEGER)),
                Arguments.of(MIN_CONTAINS, Set.of(INTEGER)),
                Arguments.of(MAX_PROPERTIES, Set.of(INTEGER)),
                Arguments.of(MIN_PROPERTIES, Set.of(INTEGER)),
                Arguments.of(REQUIRED, Set.of(ARRAY)),
                Arguments.of(DEPENDENT_REQUIRED, Set.of(OBJECT)),

                Arguments.of(PREFIX_ITEMS, Set.of(ARRAY)),
                Arguments.of(ITEMS, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(CONTAINS, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(ADDITIONAL_PROPERTIES, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(PROPERTIES, Set.of(OBJECT)),
                Arguments.of(PATTERN_PROPERTIES, Set.of(OBJECT)),
                Arguments.of(DEPENDENT_SCHEMAS, Set.of(OBJECT)),
                Arguments.of(PROPERTY_NAMES, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(IF, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(ALL_OF, Set.of(ARRAY)),
                Arguments.of(ANY_OF, Set.of(ARRAY)),
                Arguments.of(ONE_OF, Set.of(ARRAY)),
                Arguments.of(NOT, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(UNEVALUATED_ITEMS, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(UNEVALUATED_PROPERTIES, Set.of(BOOLEAN, OBJECT)),
                Arguments.of(REF, Set.of(STRING)),
                Arguments.of(DYNAMIC_REF, Set.of(STRING))
        );
    }

    private static Stream<String> getUnsupportedKeywords() {
        return Stream.of(RECURSIVE_REF, ADDITIONAL_ITEMS);
    }

    private static Stream<String> getIgnoredKeywords() {
        return Stream.of(ID, SCHEMA, ANCHOR, DYNAMIC_ANCHOR, VOCABULARY, COMMENT, DEFS, THEN, ELSE);
    }
}