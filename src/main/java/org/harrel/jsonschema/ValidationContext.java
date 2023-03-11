package org.harrel.jsonschema;

import java.net.URI;
import java.util.*;

public class ValidationContext extends AbstractContext {
    private final IdentifiableSchema parentSchema;
    private final SchemaRegistry schemaRegistry;
    private final List<Annotation> annotations;
    public final LinkedList<ValidationContext> ctxes;

    ValidationContext(URI baseUri, LinkedList<ValidationContext> ctxes, IdentifiableSchema parentSchema, SchemaRegistry schemaRegistry, List<Annotation> annotations) {
        super(baseUri);
        this.ctxes = ctxes;
        this.parentSchema = parentSchema;
        this.schemaRegistry = schemaRegistry;
        this.annotations = annotations;
    }

    public ValidationContext withParentSchema(IdentifiableSchema parentSchema) {
        return new ValidationContext(baseUri, ctxes, parentSchema, schemaRegistry, annotations);
    }

    public ValidationContext withEmptyAnnotations() {
        return new ValidationContext(baseUri, ctxes, parentSchema, schemaRegistry, new ArrayList<>());
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
        return Optional.ofNullable(schemaRegistry.get(resolvedUri))
                .or(() -> Optional.ofNullable(schemaRegistry.temp.get(resolvedUri)));
    }

    public Optional<Schema> resolveDynamicSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(parentSchema.getUri(), ref);
        if (schemaRegistry.get(resolvedUri) != null) {
            return Optional.of(schemaRegistry.get(resolvedUri));
        }
        Schema schema = null;
        Optional<String> anchor = UriUtil.getAnchor(ref);
        if (anchor.isPresent()) {
            for (ValidationContext ctx : ctxes) {
                Schema newSchema = schemaRegistry.temp.get(ctx.parentSchema.getUri().toString());
                if (newSchema != null) {
                    schema = newSchema;
                }
            }
        }
        return Optional.ofNullable(schema);
    }

    public Schema resolveRequiredSchema(String ref) {
        return Optional.ofNullable(schemaRegistry.get(ref))
                .orElseThrow(() -> new IllegalStateException("Resolution of schema (%s) failed and was required".formatted(ref)));
    }
}
