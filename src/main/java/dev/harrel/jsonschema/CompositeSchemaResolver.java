package dev.harrel.jsonschema;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * {@code CompositeSchemaResolver} class aggregates multiple {@link SchemaResolver}s into one.
 * First non-empty resolution from the aggregated {@link SchemaResolver}s will be returned.
 */
public final class CompositeSchemaResolver implements SchemaResolver {

    private final List<SchemaResolver> resolvers;

    private CompositeSchemaResolver(List<SchemaResolver> resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Factory method for composing multiple resolvers.
     */
    public static CompositeSchemaResolver of(SchemaResolver... resolvers) {
        return new CompositeSchemaResolver(unmodifiableList(asList(resolvers)));
    }

    @Override
    public Result resolve(String uri) {
        return resolvers.stream()
                .map(resolver -> resolver.resolve(uri))
                .filter(result -> !result.isEmpty())
                .findFirst()
                .orElse(Result.empty());
    }
}
