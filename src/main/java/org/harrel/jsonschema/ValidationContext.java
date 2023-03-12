package org.harrel.jsonschema;

import java.net.URI;
import java.util.*;

public class ValidationContext extends AbstractContext {
    final LinkedList<IdentifiableSchema> dynamicScope;
    private final SchemaRegistry schemaRegistry;
    private final List<Annotation> annotations;

    ValidationContext(URI baseUri, LinkedList<IdentifiableSchema> dynamicScope, SchemaRegistry schemaRegistry, List<Annotation> annotations) {
        super(baseUri);
        this.dynamicScope = dynamicScope;
        this.schemaRegistry = schemaRegistry;
        this.annotations = annotations;
    }

    public ValidationContext withEmptyAnnotations() {
        return new ValidationContext(baseUri, dynamicScope, schemaRegistry, new ArrayList<>());
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
        String resolvedUri = UriUtil.resolveUri(dynamicScope.peek().getUri(), ref);
        return Optional.ofNullable(schemaRegistry.get(resolvedUri))
                .or(() -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri)));
    }

    public Optional<Schema> resolveDynamicSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(dynamicScope.peek().getUri(), ref);
        if (schemaRegistry.get(resolvedUri) != null) {
            return Optional.of(schemaRegistry.get(resolvedUri));
        }
        Optional<String> anchor = UriUtil.getAnchor(ref);
        if (anchor.isPresent()) {
            Iterator<IdentifiableSchema> it = dynamicScope.descendingIterator();
            while (it.hasNext()) {
                Schema schema = schemaRegistry.getDynamic(it.next().getUri().toString() + "#" + anchor.get());
                if (schema != null) {
                    return Optional.of(schema);
                }
            }
        }
        return Optional.empty();
    }

    public Schema resolveRequiredSchema(String ref) {
        return Optional.ofNullable(schemaRegistry.get(ref))
                .orElseThrow(() -> new IllegalStateException("Resolution of schema (%s) failed and was required".formatted(ref)));
    }
}
