package org.harrel.jsonschema;

import java.util.Optional;

public interface SchemaResolver {
    Optional<String> resolve(String uri);
}
