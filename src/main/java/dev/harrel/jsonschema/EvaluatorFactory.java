package dev.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.*;

/**
 * {@code EvaluatorFactory} interface is responsible for creation of {@link Evaluator} instances.
 * It should hold information about keyword relation with {@link Evaluator}.
 */
public interface EvaluatorFactory {
    /**
     * This method will be invoked for each JSON object field during schema parsing process.
     * Must not throw any exceptions.
     *
     * @param ctx       current schema parsing context
     * @param fieldName field name (keyword) in JSON object for which {@link Evaluator} should be created
     * @param fieldNode value of field in JSON object
     * @return If this factory supports given field name (keyword) it should return corresponding {@link Evaluator}
     * wrapped in {@link Optional}, {@code Optional.empty()} otherwise.
     */
    Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode);

    /**
     * Composes multiple {@link EvaluatorFactory} instances into one.
     * Calls {@link EvaluatorFactory#create(SchemaParsingContext, String, JsonNode)} on each instance until non-empty
     * {@code Optional} is returned.
     * @param factories EvaluatorFactories to be composed
     * @return composed EvaluatorFactory
     */
    static EvaluatorFactory compose(EvaluatorFactory... factories) {
        return new CompositeEvaluatorFactory(factories);
    }

    /**
     * Builder for constructing {@link EvaluatorFactory} instances which is an alternative for providing a custom implementation.
     * It is recommended when the {@code Evaluator} creation logic is simple and depends only on keyword name.
     * To create factory which supports only "custom" and "type" keywords, you can write following code:
     * <pre>
     *     new EvaluatorFactory.Builder()
     *          .withKeyword("custom", CustomEvaluator::new)
     *          .withKeyword("type", CustomTypeEvaluator::new)
     *          .build();
     * </pre>
     */
    final class Builder {
        private final Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> providers = new HashMap<>();

        /**
         * Registers an evaluator with a given keyword.
         * @implNote As creating an evaluator might depend on keyword value (or type), provided function might return null
         * or throw an exception to cancel registration process.
         * @param keyword           field name in JSON schema object
         * @param evaluatorProvider function that creates {@code Evaluator} instance. Invoked only during schema parsing process.
         *                          Can return null or throw an exception, which will cancel the registration process.
         * @return this
         */
        public Builder withKeyword(String keyword, BiFunction<SchemaParsingContext, JsonNode, Evaluator> evaluatorProvider) {
            providers.put(Objects.requireNonNull(keyword), Objects.requireNonNull(evaluatorProvider));
            return this;
        }

        /**
         * @see EvaluatorFactory.Builder#withKeyword(String, BiFunction)
         */
        public Builder withKeyword(String keyword, Function<JsonNode, Evaluator> evaluatorProvider) {
            return withKeyword(keyword, (ctx, jsonNode) -> evaluatorProvider.apply(jsonNode));
        }

        /**
         * @see EvaluatorFactory.Builder#withKeyword(String, BiFunction)
         */
        public Builder withKeyword(String keyword, Supplier<Evaluator> evaluatorProvider) {
            return withKeyword(keyword, (ctx, jsonNode) -> evaluatorProvider.get());
        }

        /**
         * Creates factory instance.
         * @return factory instance
         */
        public EvaluatorFactory build() {
            return new MapEvaluatorFactory(providers);
        }
    }
}

final class CompositeEvaluatorFactory implements EvaluatorFactory {
    private final EvaluatorFactory[] factories;

    CompositeEvaluatorFactory(EvaluatorFactory[] factories) {
        this.factories = factories;
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode) {
        return Arrays.stream(factories)
                .map(factory -> factory.create(ctx, fieldName, fieldNode))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}

final class MapEvaluatorFactory implements EvaluatorFactory {
    private final Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> providers;

    MapEvaluatorFactory(Map<String, BiFunction<SchemaParsingContext, JsonNode, Evaluator>> providers) {
        this.providers = unmodifiableMap(new HashMap<>(providers));
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode) {
        BiFunction<SchemaParsingContext, JsonNode, Evaluator> provider = providers.get(fieldName);
        if (provider == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(provider.apply(ctx, fieldNode));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
