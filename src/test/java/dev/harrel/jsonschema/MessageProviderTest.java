package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageProviderTest {

    @Test
    void shouldUseCustomMessageProvider() {
        String schema = """
                {
                  "test": null,
                  "type": "null"
                }""";
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
                .withKeyword("test", () -> (ctx, node) -> Evaluator.Result.formattedFailure("%s is %d years old", "Sam", node.asInteger()))
                .build();
        Validator.Result res = new ValidatorFactory()
                .withMessageProvider(String::formatted)
                .withEvaluatorFactory(factory)
                .validate(schema, "21");

        assertThat(res.isValid()).isFalse();
        assertThat(res.getErrors()).hasSize(2);
        assertThat(res.getErrors().get(0).getError()).isEqualTo("Sam is 21 years old");
        assertThat(res.getErrors().get(1).getError()).isEqualTo("type");
    }

    @Test
    void shouldNotRunMessageProviderForIntermittentErrors() {
        String schema = """
                {
                  "anyOf": [
                    {
                      "type": "null"
                    },
                    {
                      "type": "number"
                    }
                  ],
                  "fail": true
                }""";
        EvaluatorFactory factory = new EvaluatorFactory.Builder()
                .withKeyword("fail", () -> (ctx, node) -> Evaluator.Result.failure("Failed!"))
                .build();
        Validator.Result res = new ValidatorFactory()
                .withMessageProvider(((key, args) -> {throw new UnsupportedOperationException();}))
                .withEvaluatorFactory(factory)
                .validate(schema, "1");

        assertThat(res.isValid()).isFalse();
        assertThat(res.getErrors()).hasSize(1);
        assertThat(res.getErrors().getFirst().getError()).isEqualTo("Failed!");
    }
}
