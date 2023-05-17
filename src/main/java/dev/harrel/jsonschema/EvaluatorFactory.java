package dev.harrel.jsonschema;

import java.util.Optional;

/**
 * {@code EvaluatorFactory} interface is responsible for creation of {@link Evaluator} instances.
 * It should hold information about keyword relation with {@link Evaluator}.
 */
public interface EvaluatorFactory {
    /**
     * This method will be invoked for each JSON object field during schema parsing process.
     * Must not throw any exceptions.
     * @param ctx current schema parsing context
     * @param fieldName field name (keyword) in JSON object for which {@link Evaluator} should be created
     * @param fieldNode value of field in JSON object
     * @return If this factory supports given field name (keyword) it should return corresponding {@link Evaluator}
     * wrapped in {@link Optional}, {@code Optional.empty()} otherwise.
     */
    Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode);
}
