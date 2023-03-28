package org.harrel.jsonschema;

import org.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.harrel.jsonschema.SimpleType.*;

class CoreValidatorFactoryTest {

    private static final Map<SimpleType, String> TYPE_MAP = Map.of(
            NULL, "null",
            BOOLEAN, "true",
            STRING, "\"stringValue\"",
            INTEGER, "1",
            NUMBER, "1.1",
            ARRAY, "[]",
            OBJECT, "{}"
    );

    @ParameterizedTest
    @MethodSource("getKeywords")
    void name(String keyword, Set<SimpleType> supportedTypes) {
        CoreValidatorFactory validatorFactory = new CoreValidatorFactory();
        JsonNodeFactory nodeFactory = new JacksonNode.Factory();
        SchemaParsingContext ctx = new SchemaParsingContext(new SchemaRegistry(), "CoreValidatorFactoryTest");

        for (var entry : TYPE_MAP.entrySet()) {
            Optional<Validator> validator = validatorFactory.create(ctx, keyword, nodeFactory.create(entry.getValue()));
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
                Arguments.of(Keyword.MULTIPLE_OF, Set.of(INTEGER, NUMBER))
        );
    }
}