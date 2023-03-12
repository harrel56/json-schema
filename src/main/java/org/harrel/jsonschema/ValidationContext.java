package org.harrel.jsonschema;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class ValidationContext extends AbstractContext {
    private final JsonParser jsonParser;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;
    final LinkedList<IdentifiableSchema> dynamicScope;
    private final List<Annotation> annotations;

    private ValidationContext(URI baseUri,
                              JsonParser jsonParser,
                              SchemaRegistry schemaRegistry,
                              SchemaResolver schemaResolver,
                              LinkedList<IdentifiableSchema> dynamicScope,
                              List<Annotation> annotations) {
        super(baseUri);
        this.jsonParser = jsonParser;
        this.schemaRegistry = schemaRegistry;
        this.schemaResolver = schemaResolver;
        this.dynamicScope = dynamicScope;
        this.annotations = annotations;
    }

    ValidationContext(URI baseUri, JsonParser jsonParser, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        this(baseUri, jsonParser, schemaRegistry, schemaResolver, new LinkedList<>(), new ArrayList<>());
    }

    public ValidationContext withEmptyAnnotations() {
        return new ValidationContext(baseUri, jsonParser, schemaRegistry, schemaResolver, dynamicScope, new ArrayList<>());
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
