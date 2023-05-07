package dev.harrel.jsonschema;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * {@code SchemaResolver} interface is the main abstraction for external schemas' resolution.
 */
public interface SchemaResolver {
    /**
     * Resolves URI string to schema representation
     * @param uri URI to be used for resolution
     * @return {@link Result} which contains resolved schema or {@code Result.empty}
     */
    Result resolve(String uri);

    /**
     * {@code Result} class represents schema resolution outcome.
     * It can be in one of the following states:
     * <ul>
     * <li>empty - resolution unsuccessful</li>
     * <li>containing raw JSON string</li>
     * <li>containing JSON provider node</li>
     * <li>containing {@link JsonNode}</li>
     * </ul>
     * @see SchemaResolver
     */
    final class Result {
        private final Function<JsonNodeFactory, JsonNode> toNodeFunction;

        private Result(Function<JsonNodeFactory, JsonNode> toNodeFunction) {
            this.toNodeFunction = toNodeFunction;
        }

        /**
         * Factory method for empty result.
         * @return empty resolution result
         */
        public static Result empty() {
            return new Result(null);
        }

        /**
         * Factory method for raw JSON string
         * @param rawSchema schema JSON string
         * @return resolution result based on raw JSON string
         */
        public static Result fromString(String rawSchema) {
            Objects.requireNonNull(rawSchema);
            return new Result(factory -> factory.create(rawSchema));
        }

        /**
         * Factory method for JSON provider node
         * @param schemaProviderNode JSON provider node
         * @return resolution result based on JSON provider node
         */
        public static Result fromProviderNode(Object schemaProviderNode) {
            Objects.requireNonNull(schemaProviderNode);
            return new Result(factory -> factory.wrap(schemaProviderNode));
        }

        /**
         * Factory method for {@link JsonNode}
         * @param schemaNode {@link JsonNode}
         * @return resolution result based on {@link JsonNode}
         */
        public static Result fromJsonNode(JsonNode schemaNode) {
            Objects.requireNonNull(schemaNode);
            return new Result(factory -> schemaNode);
        }

        boolean isEmpty() {
            return toNodeFunction == null;
        }

        Optional<JsonNode> toJsonNode(JsonNodeFactory factory) {
            return isEmpty() ? Optional.empty() : Optional.of(toNodeFunction.apply(factory));
        }
    }
}
