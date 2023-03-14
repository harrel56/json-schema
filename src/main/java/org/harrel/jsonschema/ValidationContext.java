package org.harrel.jsonschema;

import java.io.IOException;
import java.util.*;

public class ValidationContext {
    private final JsonParser jsonParser;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;
    private final LinkedList<IdentifiableSchema> dynamicScope;
    private final List<Annotation> annotations;
    private final List<Annotation> validationAnnotations;

    private ValidationContext(JsonParser jsonParser,
                              SchemaRegistry schemaRegistry,
                              SchemaResolver schemaResolver,
                              LinkedList<IdentifiableSchema> dynamicScope,
                              List<Annotation> annotations,
                              List<Annotation> validationAnnotations) {
        this.jsonParser = jsonParser;
        this.schemaRegistry = schemaRegistry;
        this.schemaResolver = schemaResolver;
        this.dynamicScope = dynamicScope;
        this.annotations = annotations;
        this.validationAnnotations = validationAnnotations;
    }

    ValidationContext(JsonParser jsonParser, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        this(jsonParser, schemaRegistry, schemaResolver, new LinkedList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public List<Annotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
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

    public Optional<Schema> resolveSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(dynamicScope.peek().getUri(), ref);
        return Optional.ofNullable(schemaRegistry.get(resolvedUri))
                .or(() -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri)))
                .or(() -> resolveExternalSchema(resolvedUri));
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

    void pushDynamicScope(IdentifiableSchema identifiableSchema) {
        dynamicScope.push(identifiableSchema);
    }

    void popDynamicContext() {
        dynamicScope.pop();
    }

    private Optional<Schema> resolveExternalSchema(String uri) {
        Optional<String> rawJson = schemaResolver.resolve(uri);
        if (rawJson.isPresent()) {
            try {
                jsonParser.parseRootSchema(uri, rawJson.get());
                return resolveSchema(uri);
            } catch (IOException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
