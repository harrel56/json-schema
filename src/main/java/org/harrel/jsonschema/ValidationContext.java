package org.harrel.jsonschema;

import java.net.URI;
import java.util.*;

public class ValidationContext extends AbstractContext {
    private final IdentifiableSchema parentSchema;
    private final SchemaRegistry schemaRegistry;
    private final List<Annotation> annotations;

    ValidationContext(URI baseUri, IdentifiableSchema parentSchema, SchemaRegistry schemaRegistry, List<Annotation> annotations) {
        super(baseUri);
        this.parentSchema = parentSchema;
        this.schemaRegistry = schemaRegistry;
        this.annotations = annotations;
    }

    public ValidationContext withParentSchema(IdentifiableSchema parentSchema) {
        return new ValidationContext(baseUri, parentSchema, schemaRegistry, annotations);
    }

    public ValidationContext withEmptyAnnotations() {
        return new ValidationContext(baseUri, parentSchema, schemaRegistry, new ArrayList<>());
    }

    public List<Annotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
    }

    public void addAnnotations(Collection<Annotation> annotations) {
        this.annotations.addAll(annotations);
    }

    public Optional<Schema> resolveSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(parentSchema.getUri(), ref);
        return Optional.ofNullable(schemaRegistry.get(resolvedUri));
    }

    public Optional<Schema> resolveDynamicSchema(String ref) {
        Optional<Schema> dynamicSchema = UriUtil.getAnchor(ref)
                .map(schemaRegistry::getByDynamicAnchor);
        if (dynamicSchema.isPresent()) {
            return dynamicSchema;
        }
        String resolvedUri = UriUtil.resolveUri(parentSchema.getUri(), ref);
        return Optional.ofNullable(schemaRegistry.get(resolvedUri));
    }

    public Schema resolveRequiredSchema(String ref) {
        return Optional.ofNullable(schemaRegistry.get(ref))
                .orElseThrow(() -> new IllegalStateException("Resolution of schema (%s) failed and was required".formatted(ref)));
    }
}
