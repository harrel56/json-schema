package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMessagesTest {
    @ParameterizedTest
    @MethodSource("validators")
    void validatorErrorMessages(String schema, String instance, String msg) {
        Validator.Result res = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .validate(schema, instance);
        assertThat(res.isValid()).isFalse();
        assertThat(res.getErrors()).hasSize(1);
        assertThat(res.getErrors().getFirst().getError()).isEqualTo(msg);
    }

    @ParameterizedTest
    @MethodSource("applicators")
    void applicatorErrorMessages(String schema, String instance, String msg) {
        Validator.Result res = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .validate(schema, instance);
        assertThat(res.isValid()).isFalse();
        assertThat(res.getErrors().getLast().getError()).isEqualTo(msg);
    }

    private static Stream<Arguments> validators() {
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
                        {"maximum": 10.555555}""", "10.555556", "10.555556 is greater than 10.555555"),
                Arguments.argumentSet("exclusiveMaximum (int + int)", """
                        {"exclusiveMaximum": 10}""", "10", "10 is greater than or equal to 10"),
                Arguments.argumentSet("exclusiveMaximum (float + int)", """
                        {"exclusiveMaximum": 10.99}""", "11", "11 is greater than or equal to 10.99"),
                Arguments.argumentSet("exclusiveMaximum (int + float)", """
                        {"exclusiveMaximum": 10}""", "10.0001", "10.0001 is greater than or equal to 10"),
                Arguments.argumentSet("exclusiveMaximum (float + float + int)", """
                        {"exclusiveMaximum": 10.555556}""", "10.555556", "10.555556 is greater than or equal to 10.555556"),
                Arguments.argumentSet("minimum (int + int)", """
                        {"minimum": 11}""", "10", "10 is less than 11"),
                Arguments.argumentSet("minimum (float + int)", """
                        {"minimum": 10.0001}""", "10", "10 is less than 10.0001"),
                Arguments.argumentSet("minimum (int + float)", """
                        {"minimum": 10}""", "9.9999", "9.9999 is less than 10"),
                Arguments.argumentSet("minimum (float + float + int)", """
                        {"minimum": 10.555555}""", "10.555554", "10.555554 is less than 10.555555"),
                Arguments.argumentSet("exclusiveMinimum (int + int)", """
                        {"exclusiveMinimum": 10}""", "10", "10 is less than or equal to 10"),
                Arguments.argumentSet("exclusiveMinimum (float + int)", """
                        {"exclusiveMinimum": 10.0001}""", "10", "10 is less than or equal to 10.0001"),
                Arguments.argumentSet("exclusiveMinimum (int + float)", """
                        {"exclusiveMinimum": 10}""", "9.9999", "9.9999 is less than or equal to 10"),
                Arguments.argumentSet("exclusiveMinimum (float + float + int)", """
                        {"exclusiveMinimum": 10.555555}""", "10.555555", "10.555555 is less than or equal to 10.555555"),
                Arguments.argumentSet("maxLength", """
                        {"maxLength": 4}""", "\"hello\"", "\"hello\" is longer than 4 characters"),
                Arguments.argumentSet("minLength", """
                        {"minLength": 4}""", "\"hi\"", "\"hi\" is shorter than 4 characters"),
                Arguments.argumentSet("pattern", """
                        {"pattern": "x{3,4}"}""", "\"xx\"", "\"xx\" does not match regular expression x{3,4}"),
                Arguments.argumentSet("maxItems", """
                        {"maxItems": 2}""", "[1, 2, 3]", "Array has more than 2 items"),
                Arguments.argumentSet("minItems", """
                        {"minItems": 2}""", "[1]", "Array has less than 2 items"),
                Arguments.argumentSet("uniqueItems", """
                        {"uniqueItems": true}""", "[1, 1]", "Array contains non-unique item at index 1"),
                Arguments.argumentSet("maxContains", """
                        {"maxContains": 0, "contains": {"type": "number"}}""", "[1]", "Array contains more than 0 matching items"),
                Arguments.argumentSet("minContains", """
                        {"minContains": 2, "contains": {"type": "number"}}""", "[1]", "Array contains less than 2 matching items"),
                Arguments.argumentSet("maxProperties", """
                        {"maxProperties": 1}""", "{\"a\": 1, \"b\": 2}", "Object has more than 1 properties"),
                Arguments.argumentSet("minProperties", """
                        {"minProperties": 1}""", "{}", "Object has less than 1 properties"),
                Arguments.argumentSet("required", """
                        {"required": ["a"]}""", "{}", "Object does not have some of the required properties [a]"),
                Arguments.argumentSet("dependentRequired", """
                        {"dependentRequired": {"a": ["b", "c"]}}""", "{\"a\": 1, \"c\": 3}", "Object does not have some of the required properties [b]")
        );
    }

    private static Stream<Arguments> applicators() {
        return Stream.of(
                Arguments.argumentSet("contains", """
                        {"contains": {"type": "integer"}}""", "[true, null, 1.1]", "Array contains no matching items"),
                Arguments.argumentSet("dependentSchemas", """
                        {"dependentSchemas": {"a": false}}""", "{\"a\": 1}", "Object does not match dependent schemas for some properties [a]"),
                Arguments.argumentSet("if + then", """
                        {"if": true, "then": false}""", "null", "Value matches against schema from 'if' but does not match against schema from 'then'"),
                Arguments.argumentSet("if + else", """
                        {"if": false, "else": false}""", "null", "Value does not match against schema from 'if' and 'else'"),
                Arguments.argumentSet("allOf", """
                        {"allOf": [false, true, false]}""", "null", "Value does not match against the schemas at indexes [0, 2]"),
                Arguments.argumentSet("anyOf (0 matches)", """
                        {"anyOf": [false, false]}""", "null", "Value does not match against any of the schemas"),
                Arguments.argumentSet("oneOf (2 matches)", """
                        {"oneOf": [false, false]}""", "null", "Value does not match against any of the schemas"),
                Arguments.argumentSet("oneOf", """
                        {"oneOf": [true, false, true]}""", "null", "Value matches against more than one schema. Matched schema indexes [0, 2]"),
                Arguments.argumentSet("not", """
                        {"not": true}""", "null", "Value matches against given schema but it must not"),
                Arguments.argumentSet("$ref", """
                        {"$ref": "https://schema.com"}""", "null", "Resolution of $ref [https://schema.com] failed"),
                Arguments.argumentSet("$dynamicRef", """
                        {"$id": "https://schema.com", "$dynamicRef": "#oops"}""", "null", "Resolution of $dynamicRef [https://schema.com#oops] failed")
                // $recursiveRef: seems that it cannot fail
        );
    }
}
