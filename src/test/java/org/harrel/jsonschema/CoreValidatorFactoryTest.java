package org.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.harrel.jsonschema.Keyword.*;
import static org.harrel.jsonschema.SimpleType.*;

public abstract class CoreValidatorFactoryTest {

    protected static JsonNodeFactory nodeFactory;
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
    void shouldCreateValidatorOnlyForSupportedTypes(String keyword, Set<SimpleType> supportedTypes) {
        CoreValidatorFactory validatorFactory = new CoreValidatorFactory();
        SchemaParsingContext ctx = new SchemaParsingContext(new SchemaRegistry(), "CoreValidatorFactoryTest");

        for (var entry : TYPE_MAP.entrySet()) {
            JsonNode wrappedNode = nodeFactory.create("{\"%s\": %s}".formatted(keyword, entry.getValue()));
            Optional<Validator> validator = validatorFactory.create(ctx, keyword, wrappedNode.asObject().get(keyword));
            if (supportedTypes.contains(entry.getKey())) {
                assertThat(validator)
                        .withFailMessage("Expected type [%s] to pass", entry.getKey())
                        .isPresent();
            } else {
                assertThat(validator)
                        .withFailMessage("Expected type [%s] to fail", entry.getKey())
                        .isEmpty();
            }
        }
    }

    private static Stream<Arguments> getKeywords() {
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
                Arguments.of(THEN, Set.of()),
                Arguments.of(ELSE, Set.of()),
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
}