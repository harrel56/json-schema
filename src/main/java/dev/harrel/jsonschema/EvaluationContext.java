package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

public final class EvaluationContext {
    private final JsonParser jsonParser;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;
    private final LinkedList<URI> dynamicScope;
    private final List<Annotation> annotations;
    private final List<Annotation> validationAnnotations;

    EvaluationContext(JsonParser jsonParser, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        this.jsonParser = jsonParser;
        this.schemaRegistry = schemaRegistry;
        this.schemaResolver = schemaResolver;
        this.dynamicScope = new LinkedList<>();
        this.annotations = new ArrayList<>();
        this.validationAnnotations = new ArrayList<>();
    }

    public List<Annotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
    }

    public Optional<Schema> resolveSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(dynamicScope.peek(), ref);
        return Optional.ofNullable(schemaRegistry.get(resolvedUri))
                .or(() -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri)))
                .or(() -> resolveExternalSchema(resolvedUri));
    }

    public Optional<Schema> resolveDynamicSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(dynamicScope.peek(), ref);
        if (schemaRegistry.get(resolvedUri) != null) {
            return Optional.of(schemaRegistry.get(resolvedUri));
        }
        Optional<String> anchor = UriUtil.getAnchor(ref);
        if (anchor.isPresent()) {
            Iterator<URI> it = dynamicScope.descendingIterator();
            while (it.hasNext()) {
                Schema schema = schemaRegistry.getDynamic(it.next().toString() + "#" + anchor.get());
                if (schema != null) {
                    return Optional.of(schema);
                }
            }
        }
        return Optional.empty();
    }

    public Schema resolveRequiredSchema(String ref) {
        return Optional.ofNullable(schemaRegistry.get(ref))
                .orElseThrow(() -> new IllegalStateException("Resolution of schema [%s] failed and was required".formatted(ref)));
    }

    boolean isOutOfDynamicScope(URI uri) {
        return dynamicScope.isEmpty() || !uri.equals(dynamicScope.peek());
    }

    void pushDynamicScope(URI uri) {
        dynamicScope.push(uri);
    }

    void popDynamicContext() {
        dynamicScope.pop();
    }

    List<Annotation> getValidationAnnotations() {
        return Collections.unmodifiableList(validationAnnotations);
    }

    void addValidationAnnotation(Annotation annotation) {
        this.validationAnnotations.add(annotation);
    }

    void truncateAnnotationsToSize(int size) {
        annotations.subList(size, annotations.size()).clear();
    }

    private Optional<Schema> resolveExternalSchema(String uri) {
        String baseUri = UriUtil.getUriWithoutFragment(uri);
        if (schemaRegistry.get(baseUri) != null) {
            return Optional.empty();
        }
        Optional<String> rawJson = schemaResolver.resolve(baseUri);
        if (rawJson.isPresent()) {
            try {
                jsonParser.parseRootSchema(baseUri, rawJson.get());
                return resolveSchema(uri);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
