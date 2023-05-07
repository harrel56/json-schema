package dev.harrel.jsonschema;

import java.util.Objects;

final class EvaluatorWrapper implements Evaluator {
    private final String keyword;
    private final String keywordPath;
    private final Evaluator evaluator;

    EvaluatorWrapper(String keyword, String keywordPath, Evaluator evaluator) {
        this.keyword = Objects.requireNonNull(keyword);
        this.keywordPath = Objects.requireNonNull(keywordPath);
        this.evaluator = Objects.requireNonNull(evaluator);
    }

    EvaluatorWrapper(String keyword, JsonNode keywordNode, Evaluator evaluator) {
        this(keyword, keywordNode.getJsonPointer(), evaluator);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode instanceNode) {
        return evaluator.evaluate(ctx, instanceNode);
    }

    @Override
    public int getOrder() {
        return evaluator.getOrder();
    }

    String getKeyword() {
        return keyword;
    }

    String getKeywordPath() {
        return keywordPath;
    }
}
