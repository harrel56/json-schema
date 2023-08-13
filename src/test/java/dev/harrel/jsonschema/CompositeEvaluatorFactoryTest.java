package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CompositeEvaluatorFactoryTest {
    private final SchemaParsingContext ctx = mock(SchemaParsingContext.class);
    private final JsonNode node = mock(JsonNode.class);

    @Test
    void shouldReturnEmptyForNoEvaluatorFactories() {
        EvaluatorFactory compositeFactory = EvaluatorFactory.compose();
        assertThat(compositeFactory.create(ctx, "x", node)).isEmpty();
    }

    @Test
    void shouldCombineMultipleEvaluatorFactories() {
        Evaluator evaluator1 = (ctx, node) -> Evaluator.Result.success();
        EvaluatorFactory factory1 = (ctx, fieldName, fieldNode) -> {
            if ("x".equals(fieldName)) {
                return Optional.of(evaluator1);
            } else {
                return Optional.empty();
            }
        };
        Evaluator evaluator2 = (ctx, node) -> Evaluator.Result.success();
        EvaluatorFactory factory2 = (ctx, fieldName, fieldNode) -> {
            if ("y".equals(fieldName)) {
                return Optional.of(evaluator2);
            } else {
                return Optional.empty();
            }
        };
        Evaluator evaluator3 = (ctx, node) -> Evaluator.Result.success();
        EvaluatorFactory factory3 = (ctx, fieldName, fieldNode) -> {
            if ("z".equals(fieldName)) {
                return Optional.of(evaluator3);
            } else {
                return Optional.empty();
            }
        };

        EvaluatorFactory compositeFactory = EvaluatorFactory.compose(factory1, factory2, factory3);
        assertThat(compositeFactory.create(ctx, "x", node)).hasValue(evaluator1);
        assertThat(compositeFactory.create(ctx, "y", node)).hasValue(evaluator2);
        assertThat(compositeFactory.create(ctx, "z", node)).hasValue(evaluator3);
        assertThat(compositeFactory.create(ctx, "a", node)).isEmpty();
    }

    @Test
    void shouldReturnFirstResolvedEvaluatorFactory() {
        Evaluator evaluator1 = (ctx, node) -> Evaluator.Result.success();
        EvaluatorFactory factory1 = (ctx, fieldName, fieldNode) -> {
            if ("x".equals(fieldName)) {
                return Optional.of(evaluator1);
            } else {
                return Optional.empty();
            }
        };
        Evaluator evaluator2 = (ctx, node) -> Evaluator.Result.success();
        EvaluatorFactory factory2 = (ctx, fieldName, fieldNode) -> {
            if ("x".equals(fieldName)) {
                return Optional.of(evaluator2);
            } else {
                return Optional.empty();
            }
        };
        Evaluator evaluator3 = (ctx, node) -> Evaluator.Result.success();
        EvaluatorFactory factory3 = (ctx, fieldName, fieldNode) -> {
            if ("x".equals(fieldName)) {
                return Optional.of(evaluator3);
            } else {
                return Optional.empty();
            }
        };

        EvaluatorFactory compositeFactory = EvaluatorFactory.compose(factory1, factory2, factory3);
        assertThat(compositeFactory.create(ctx, "x", node)).hasValue(evaluator1);
    }
}