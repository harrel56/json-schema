package dev.harrel.jsonschema;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public final class CompositeSchemaResolver implements SchemaResolver {

    private final List<SchemaResolver> resolvers;

    private CompositeSchemaResolver(List<SchemaResolver> resolvers) {
        this.resolvers = resolvers;
    }

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
