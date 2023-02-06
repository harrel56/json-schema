package org.harrel.jsonschema;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

public class ValidationContext {
    private final URI baseUri;
    private final Map<String, Schema> schemaCache;

    public ValidationContext(URI baseUri, Map<String, Schema> schemaCache) {
        this.baseUri = baseUri;
        this.schemaCache = schemaCache;
    }

    public Optional<Schema> resolveSchema(String ref) {
        return Optional.ofNullable(schemaCache.get(ref));
    }

    public Schema resolveRequiredSchema(String ref) {
        return resolveSchema(ref)
                .orElseThrow(() -> new IllegalStateException("Resolution of $ref (%s) failed and was required".formatted(ref)));
    }
}
