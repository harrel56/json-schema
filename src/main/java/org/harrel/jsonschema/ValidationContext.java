package org.harrel.jsonschema;

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
        return resolveSchema(ref)
                .orElseThrow(() -> new IllegalStateException("Resolution of schema (%s) failed and was required".formatted(ref)));
    }

    private String resolveUri(String ref) {
        if (ref.equals("#")) {
            return parentSchema.getId();
        } else if (UriUtil.isRelativeJsonPoint(ref)) {
            return parentSchema.getId() + ref;
        } else {
            return ref;
        }
    }
}
