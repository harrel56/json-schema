package dev.harrel.jsonschema;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

final class CompositeEvaluatorFactory implements EvaluatorFactory {
    private final List<EvaluatorFactory> factories;

    private CompositeEvaluatorFactory(List<EvaluatorFactory> factories) {
        this.factories = factories;
    }

    static CompositeEvaluatorFactory of(EvaluatorFactory... factories) {
        return new CompositeEvaluatorFactory(unmodifiableList(asList(factories)));
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode) {
        return factories.stream()
                .map(factory -> factory.create(ctx, fieldName, fieldNode))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
