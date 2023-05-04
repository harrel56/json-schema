package dev.harrel.jsonschema;

import java.util.Optional;

public interface EvaluatorFactory {
    Optional<Evaluator> create(SchemaParsingContext context, String fieldName, JsonNode fieldNode);
}
