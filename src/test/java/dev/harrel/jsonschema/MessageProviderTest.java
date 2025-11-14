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
}
