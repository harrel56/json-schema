package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EvaluatorWrapperTest {

    @Test
    void shouldReturnKeyword() {
        EvaluatorWrapper wrapper = new EvaluatorWrapper("keyword", "keywordPath", (ctx, node) -> Evaluator.Result.success());
        assertThat(wrapper.getKeyword()).isEqualTo("keyword");
    }

    @Test
    void shouldReturnKeywordPath() {
        EvaluatorWrapper wrapper = new EvaluatorWrapper("keyword", "keywordPath", (ctx, node) -> Evaluator.Result.success());
        assertThat(wrapper.getKeywordPath()).isEqualTo("keywordPath");
    }

    @Test
    void shouldDelegateEvaluatorOrder() {
        Evaluator evaluator = new Evaluator() {
            @Override
            public Result evaluate(EvaluationContext ctx, JsonNode node) {
                return Result.success();
            }
            @Override
            public int getOrder() {
                return 123;
            }
        };
        EvaluatorWrapper wrapper = new EvaluatorWrapper("keyword", "keywordPath", evaluator);
        assertThat(wrapper.getOrder()).isEqualTo(123);
    }

    @Test
    void shouldDelegateEvaluatorEvaluate() {
        Evaluator evaluator = (ctx, node) -> Evaluator.Result.failure(String.valueOf(Objects.hash(ctx, node)));
        EvaluatorWrapper wrapper = new EvaluatorWrapper("keyword", "keywordPath", evaluator);
        EvaluationContext ctx = mock(EvaluationContext.class);
        JsonNode node = mock(JsonNode.class);
        Evaluator.Result result = wrapper.evaluate(ctx, node);
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getError()).isEqualTo(String.valueOf(Objects.hash(ctx, node)));
    }
}