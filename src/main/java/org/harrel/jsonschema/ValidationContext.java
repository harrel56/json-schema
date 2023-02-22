package org.harrel.jsonschema;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

public class ValidationContext {
    private final IdentifiableSchema parentSchema;
    private final Map<String, Schema> schemaCache;

    public ValidationContext(IdentifiableSchema parentSchema, Map<String, Schema> schemaCache) {
        this.parentSchema = parentSchema;
        this.schemaCache = schemaCache;
    }

    public ValidationContext withParentSchema(IdentifiableSchema parentSchema) {
        return new ValidationContext(parentSchema, schemaCache);
    }

    public Optional<Schema> resolveSchema(String ref) {
        String resolvedUri = resolveUri(ref);
        return Optional.ofNullable(schemaCache.get(resolvedUri));
    }

    public Schema resolveRequiredSchema(String ref) {
        return Optional.ofNullable(schemaCache.get(ref))
                .orElseThrow(() -> new IllegalStateException("Resolution of schema (%s) failed and was required".formatted(ref)));
    }

    private String resolveUri(String ref) {
        URI baseUri = parentSchema.getUri();
        ref = UriUtil.decodeUrl(ref);
        if (baseUri.getAuthority() == null && UriUtil.isJsonPointer(ref)) {
            return baseUri + ref;
        }
        if (ref.equals("#")) {
            return baseUri.toString();
        } else if (UriUtil.isJsonPointer(ref)) {
            return baseUri + ref;
        } else {
            return baseUri.resolve(ref).toString();
        }
    }
}
