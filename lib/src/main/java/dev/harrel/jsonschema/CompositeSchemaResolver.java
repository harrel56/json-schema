package dev.harrel.jsonschema;

import java.util.Arrays;

/**
 * {@code CompositeSchemaResolver} class aggregates multiple {@link SchemaResolver}s into one.
 * First non-empty resolution from the aggregated {@link SchemaResolver}s will be returned.
 * @deprecated since version 1.2.1, subject to removal in future releases.
 * Please use {@link SchemaResolver#compose(SchemaResolver...)} instead.
 */
@Deprecated
public final class CompositeSchemaResolver implements SchemaResolver {
    private final SchemaResolver[] resolvers;

    private CompositeSchemaResolver(SchemaResolver[] resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Factory method for composing multiple resolvers.
     * @deprecated since version 1.2.1, subject to removal in future releases.
     * Please use {@link SchemaResolver#compose(SchemaResolver...)} instead.
     */
    @Deprecated
    public static CompositeSchemaResolver of(SchemaResolver... resolvers) {
        return new CompositeSchemaResolver(resolvers);
    }

    @Override
    public Result resolve(String uri) {
        return Arrays.stream(resolvers)
                .map(resolver -> resolver.resolve(uri))
                .filter(result -> !result.isEmpty())
                .findFirst()
                .orElse(Result.empty());
    }
}
