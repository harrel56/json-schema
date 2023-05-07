package dev.harrel.jsonschema;

/**
 * {@code JsonNodeFactory} interface is the main abstraction for provider-agnostic JSON factory.
 */
public interface JsonNodeFactory {
    /**
     * Wraps provider specific JSON node into {@link JsonNode}.
     * Main purpose of this method is to avoid additional JSON parsing.
     * @param node provider specific representation of JSON node
     * @return wrapped node
     * @throws RuntimeException when provided node is of invalid type
     */
    JsonNode wrap(Object node);

    /**
     * Creates {@link JsonNode} from raw JSON string.
     * @param rawJson JSON in string form
     * @return created node
     * @throws RuntimeException when creation fails for any reasons (e.g. provided string was not a valid JSON)
     */
    JsonNode create(String rawJson);
}
