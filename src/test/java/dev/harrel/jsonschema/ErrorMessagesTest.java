package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMessagesTest {
    @ParameterizedTest
    @MethodSource("evaluators")
    void evaluatorErrorMessages(String schema, String instance, String msg) {
        Validator.Result res = new ValidatorFactory().validate(schema, instance);
        assertThat(res.isValid()).isFalse();
        assertThat(res.getErrors()).hasSize(1);
        assertThat(res.getErrors().getFirst().getError()).isEqualTo(msg);
    }

    private static Stream<Arguments> evaluators() {
        return Stream.of(
                Arguments.argumentSet("type", """
                        {"type": "number"}""", "{}", "Value is [object] but should be [number]"),
                Arguments.argumentSet("type (array)", """
                        {"type": ["array", "number"]}""", "{}", "Value is [object] but should be [array, number]"),
                Arguments.argumentSet("const (number)", """
                        {"const": 123}""", "{}", "Expected 123"),
                Arguments.argumentSet("const (array)", """
                        {"const": [123]}""", "{}", "Expected specific array"),
                Arguments.argumentSet("const (object)", """
                        {"const": {}}""", "1", "Expected specific object"),
                Arguments.argumentSet("enum", """
                        {"enum": ["yes", "no", 0, 1, [], {}]}""", "null", "Expected any of [yes, no, 0, 1, specific array, specific object]"),
                Arguments.argumentSet("enum (single)", """
                        {"enum": ["yes"]}""", "null", "Expected any of [yes]"),
                Arguments.argumentSet("multipleOf (int + int)", """
                        {"multipleOf": 2}""", "3", "3 is not multiple of 2"),
                Arguments.argumentSet("multipleOf (float + int)", """
                        {"multipleOf": 2.1}""", "3", "3 is not multiple of 2.1"),
                Arguments.argumentSet("multipleOf (int + float)", """
                        {"multipleOf": 2}""", "3.334", "3.334 is not multiple of 2"),
                Arguments.argumentSet("multipleOf (float + float)", """
                        {"multipleOf": 1.666}""", "3.334", "3.334 is not multiple of 1.666"),
                Arguments.argumentSet("maximum (int + int)", """
                        {"maximum": 10}""", "11", "11 is greater than 10"),
                Arguments.argumentSet("maximum (float + int)", """
                        {"maximum": 10.99}""", "11", "11 is greater than 10.99"),
                Arguments.argumentSet("maximum (int + float)", """
                        {"maximum": 10}""", "10.0001", "10.0001 is greater than 10"),
                Arguments.argumentSet("maximum (float + float + int)", """
                        {"maximum": 10.555555}""", "10.555556", "10.555556 is greater than 10.555555")
        );
    }
}
