package dev.harrel.jsonschema;

public interface Evaluator extends Comparable<Evaluator> {
    EvaluationResult evaluate(EvaluationContext ctx, JsonNode node);

    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(Evaluator other) {
        return Integer.compare(getOrder(), other.getOrder());
    }
}
