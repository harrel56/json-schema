package dev.harrel.jsonschema;

import java.util.List;
import java.util.Optional;

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
    public Optional<String> resolve(String uri) {
        return resolvers.stream()
                .map(resolver -> resolver.resolve(uri))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
