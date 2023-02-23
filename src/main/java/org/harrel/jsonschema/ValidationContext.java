package org.harrel.jsonschema;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ValidationContext {
    private final AnnotationCollector<?> annotationCollector;
    private final IdentifiableSchema parentSchema;
    private final Map<String, Schema> schemaCache;

    ValidationContext(AnnotationCollector<?> annotationCollector, IdentifiableSchema parentSchema, Map<String, Schema> schemaCache) {
        this.annotationCollector = annotationCollector;
        this.parentSchema = parentSchema;
        this.schemaCache = schemaCache;
    }

    public ValidationContext withParentSchema(IdentifiableSchema parentSchema) {
        return new ValidationContext(annotationCollector, parentSchema, schemaCache);
    }

    public Set<String> getEvaluatedPaths() {
        return annotationCollector.getEvaluatedPaths();
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
