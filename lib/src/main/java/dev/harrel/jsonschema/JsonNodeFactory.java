package dev.harrel.jsonschema;

/**
 * {@code JsonNodeFactory} interface is the main abstraction for provider-agnostic JSON factory.
 */
public interface JsonNodeFactory {
    /**
     * Wraps provider specific JSON node into {@link JsonNode}.
     * The returned node is considered a root node and all further JSON pointers will be calculated from this node.
     * Main purpose of this method is to avoid additional JSON parsing.
     * It can also be used to convert any instance of {@link JsonNode} to a root node.
     *
     * @param node provider specific representation of JSON node
     * @return wrapped node
     * @throws RuntimeException when provided node is of invalid type
     */
    JsonNode wrap(Object node);

    /**
     * Creates {@link JsonNode} from raw JSON string.
     * The returned node is considered a root node and all further JSON pointers will be calculated from this node.
     *
     * @param rawJson JSON in string form
     * @return created node
     * @throws RuntimeException when creation fails for any reasons (e.g. provided string is not a valid JSON)
     */
    JsonNode create(String rawJson);
}
