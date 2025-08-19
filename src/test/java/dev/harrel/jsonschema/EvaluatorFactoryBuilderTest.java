package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static dev.harrel.jsonschema.util.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvaluatorFactoryBuilderTest {

    @Test
    void shouldNotAllowNullKeyword() {
        EvaluatorFactory.Builder builder = new EvaluatorFactory.Builder();
        assertThatThrownBy(() -> builder.withKeyword(null, TypeEvaluator::new))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullProvider() {
        EvaluatorFactory.Builder builder = new EvaluatorFactory.Builder();
        assertThatThrownBy(() -> builder.withKeyword("type", (BiFunction<SchemaParsingContext, JsonNode, Evaluator>) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldHandleEmptyFactory() {
        String schema = """
                {
                  "type": "number"
                }""";
        EvaluatorFactory factory = new EvaluatorFactory.Builder().build();

        Validator.Result result = new ValidatorFactory().withEvaluatorFactory(factory).validate(schema, "null");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/type",
                "https://harrel.dev",
                "",
                "type",
                "Value is [null] but should be [number]"
        );
    }

    @Test
    void shouldOverwriteKeyword() {
        String schema = """
                {
                  "type": "number"
                }""";
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
                .withKeyword("type", (node) -> new StringAnnotationEvaluator(node.asString()))
                .build();

        Validator.Result result = new ValidatorFactory().withEvaluatorFactory(factory).validate(schema, "null");
        assertThat(result.isValid()).isTrue();
        List<Annotation> annotations = result.getAnnotations();
        assertThat(annotations).hasSize(1);
        assertAnnotation(
                annotations.getFirst(),
                "/type",
                "https://harrel.dev",
                "",
                "type",
                "number"
        );
    }

    @Test
    void shouldOverwriteMultipleKeywords() {
        String schema = """
                {
                  "type": "number",
                  "minLength": 5
                }""";
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
                .withKeyword("type", (node) -> new StringAnnotationEvaluator(node.asString()))
                .withKeyword("minLength", (node) -> new StringAnnotationEvaluator(node.asInteger().toString()))
                .build();

        Validator.Result result = new ValidatorFactory().withEvaluatorFactory(factory).validate(schema, "\"a\"");
        assertThat(result.isValid()).isTrue();
        List<Annotation> annotations = sortAnnotations(result.getAnnotations());
        assertThat(annotations).hasSize(2);
        assertAnnotation(
                annotations.get(0),
                "/minLength",
                "https://harrel.dev",
                "",
                "minLength",
                "5"
        );
        assertAnnotation(
                annotations.get(1),
                "/type",
                "https://harrel.dev",
                "",
                "type",
                "number"
        );
    }

    @Test
    void shouldCancelRegistrationWhenNullReturned() {
        String schema = """
                {
                  "type": "number",
                  "minLength": 5
                }""";
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
                .withKeyword("type", (node) -> new StringAnnotationEvaluator(node.asString()))
                .withKeyword("minLength", () -> null)
                .build();

        Validator.Result result = new ValidatorFactory().withEvaluatorFactory(factory).validate(schema, "\"a\"");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/minLength",
                "https://harrel.dev",
                "",
                "minLength",
                "\"a\" is shorter than 5 characters"
        );
    }

    @Test
    void shouldCancelRegistrationWhenExceptionThrown() {
        String schema = """
                {
                  "type": "number",
                  "minLength": 5
                }""";
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
                .withKeyword("type", (node) -> new StringAnnotationEvaluator(node.asString()))
                .withKeyword("minLength", () -> {throw new RuntimeException("oops");})
                .build();

        Validator.Result result = new ValidatorFactory().withEvaluatorFactory(factory).validate(schema, "\"a\"");
        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.getFirst(),
                "/minLength",
                "https://harrel.dev",
                "",
                "minLength",
                "\"a\" is shorter than 5 characters"
        );
    }

    @Test
    void shouldOverwriteMultipleRegistrations() {
        String schema = """
                {
                  "type": "number"
                }""";
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
                .withKeyword("type", () -> new StringAnnotationEvaluator("reg1"))
                .withKeyword("type", () -> new StringAnnotationEvaluator("reg2"))
                .build();

        Validator.Result result = new ValidatorFactory().withEvaluatorFactory(factory).validate(schema, "\"a\"");
        assertThat(result.isValid()).isTrue();
        List<Annotation> annotations = result.getAnnotations();
        assertThat(annotations).hasSize(1);
        assertAnnotation(
                annotations.getFirst(),
                "/type",
                "https://harrel.dev",
                "",
                "type",
                "reg2"
        );
    }

    private static class StringAnnotationEvaluator implements Evaluator {
        private final String annotation;

        private StringAnnotationEvaluator(String annotation) {
            this.annotation = annotation;
        }

        @Override
        public Result evaluate(EvaluationContext ctx, JsonNode node) {
            return Result.success(annotation);
        }
    }
}