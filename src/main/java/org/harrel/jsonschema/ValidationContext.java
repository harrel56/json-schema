package org.harrel.jsonschema;

import java.util.*;

public class ValidationContext {
    private final IdentifiableSchema parentSchema;
    private final Map<String, Schema> schemaCache;
    private final List<Annotation> annotations;

    ValidationContext(IdentifiableSchema parentSchema, Map<String, Schema> schemaCache, List<Annotation> annotations) {
        this.parentSchema = parentSchema;
        this.schemaCache = schemaCache;
        this.annotations = annotations;
    }

    public ValidationContext withParentSchema(IdentifiableSchema parentSchema) {
        return new ValidationContext(parentSchema, schemaCache, annotations);
    }

    public ValidationContext withEmptyAnnotations() {
        return new ValidationContext(parentSchema, schemaCache, new ArrayList<>());
    }

    public List<Annotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    public void addAnnotations(Collection<Annotation> annotations) {
        this.annotations.addAll(annotations);
    }

    public Optional<Schema> resolveSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(parentSchema.getUri(), ref);
        return Optional.ofNullable(schemaCache.get(resolvedUri));
    }

    public Schema resolveRequiredSchema(String ref) {
        return Optional.ofNullable(schemaCache.get(ref))
                .orElseThrow(() -> new IllegalStateException("Resolution of schema (%s) failed and was required".formatted(ref)));
    }
}
