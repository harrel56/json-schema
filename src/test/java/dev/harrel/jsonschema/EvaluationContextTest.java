package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;

class EvaluationContextTest {

    @Test
    void shouldResolveRef() {
        Evaluator customEvaluator = (ctx, node) -> {
            if (ctx.resolveRefAndValidate("urn:custom#/def", node)) {
                return Evaluator.Result.success();
            } else {
                return Evaluator.Result.failure();
            }
        };
        var customFactory = new EvaluatorFactory() {
            @Override
            public Optional<Evaluator> create(SchemaParsingContext spCtx, String fieldName, JsonNode fieldNode) {
                if (fieldName.equals("custom")) {
                    return Optional.of(customEvaluator);
                }
                return Optional.empty();
            }
        };

        String schema = """
                {
                  "$id": "urn:test",
                  "custom": "don't care",
                  "something": {
                    "$id": "urn:custom",
                    "def": {
                      "type": "null"
                    }
                  }
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(customFactory)
                .validate(schema, "{}");

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/custom/type",
                "urn:test#/something/def",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }

    @Test
    void shouldResolveDynamicRef() {
        Evaluator customEvaluator = (ctx, node) -> {
            if (ctx.resolveDynamicRefAndValidate("urn:custom#/def", node)) {
                return Evaluator.Result.success();
            } else {
                return Evaluator.Result.failure();
            }
        };
        var customFactory = new EvaluatorFactory() {
            @Override
            public Optional<Evaluator> create(SchemaParsingContext spCtx, String fieldName, JsonNode fieldNode) {
                if (fieldName.equals("custom")) {
                    return Optional.of(customEvaluator);
                }
                return Optional.empty();
            }
        };

        String schema = """
                {
                  "$id": "urn:test",
                  "custom": "don't care",
                  "something": {
                    "$id": "urn:custom",
                    "def": {
                      "type": "null"
                    }
                  }
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(customFactory)
                .validate(schema, "{}");

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/custom/type",
                "urn:test#/something/def",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }

    @Test
    void shouldResolveInternalRef() {
        class CustomEvaluator implements Evaluator {
            private final String ref;

            public CustomEvaluator(SchemaParsingContext spCtx, JsonNode fieldNode) {
                this.ref = spCtx.getAbsoluteUri(fieldNode.asArray().get(0));
            }

            @Override
            public Result evaluate(EvaluationContext ctx, JsonNode node) {
                if (ctx.resolveInternalRefAndValidate(ref, node)) {
                    return Evaluator.Result.success();
                } else {
                    return Evaluator.Result.failure();
                }
            }
        };
        var customFactory = new EvaluatorFactory() {
            @Override
            public Optional<Evaluator> create(SchemaParsingContext spCtx, String fieldName, JsonNode fieldNode) {
                if (fieldName.equals("custom")) {
                    return Optional.of(new CustomEvaluator(spCtx, fieldNode));
                }
                return Optional.empty();
            }
        };

        String schema = """
                {
                  "$id": "urn:test",
                  "custom": [
                    {
                      "type": "null"
                    }
                  ]
                }""";
        Validator.Result result = new ValidatorFactory()
                .withEvaluatorFactory(customFactory)
                .validate(schema, "{}");

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(1);
        assertError(
                errors.get(0),
                "/custom/0/type",
                "urn:test#/custom/0",
                "",
                "type",
                "Value is [object] but should be [null]"
        );
    }
}