package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

/**
 * {@code EvaluationContext} class represents state of current evaluation (instance validation against schema).
 * {@link Evaluator} can use this class for its processing logic.
 * @see Evaluator
 */
public final class EvaluationContext {
    private final JsonNodeFactory jsonNodeFactory;
    private final JsonParser jsonParser;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;
    private final LinkedList<URI> dynamicScope;
    private final List<EvaluationItem> evaluationItems;
    private final List<EvaluationItem> validationItems;

    EvaluationContext(JsonNodeFactory jsonNodeFactory,
                      JsonParser jsonParser,
                      SchemaRegistry schemaRegistry,
                      SchemaResolver schemaResolver) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.jsonParser = Objects.requireNonNull(jsonParser);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
        this.dynamicScope = new LinkedList<>();
        this.evaluationItems = new ArrayList<>();
        this.validationItems = new ArrayList<>();
    }

    /**
     * Returns collected annotations up to this point.
     * Discarded annotations are not included.
     * @return unmodifiable list of annotations
     */
    public List<EvaluationItem> getEvaluationItems() {
        return Collections.unmodifiableList(evaluationItems);
    }

    /**
     * Optionally resolves reference (URI) to a schema.
     * @param ref reference to a schema
     * @return Resolved {@link Schema} wrapped in {@link Optional}, {@code Optional.empty()} otherwise
     * @see EvaluationContext#resolveRequiredSchema(String)
     */
    public Optional<Schema> resolveSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(dynamicScope.peek(), ref);
        return Optional.ofNullable(schemaRegistry.get(resolvedUri))
                .or(() -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri)))
                .or(() -> resolveExternalSchema(resolvedUri));
    }

    /**
     * Optionally resolves dynamic reference (URI) to a schema.
     * Mainly used in conjunction with <i>$dynamicRef</i> keyword.
     * @param ref dynamic reference to a schema
     * @return Resolved {@link Schema} wrapped in {@link Optional}, {@code Optional.empty()} otherwise
     */
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

    /**
     * Resolves required reference (URI) to a schema.
     * @param ref reference to a schema
     * @return Resolved {@link Schema}
     * @throws IllegalStateException when schema resolution failed
     * @see EvaluationContext#resolveSchema(String)
     */
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

    List<EvaluationItem> getValidationItems() {
        return Collections.unmodifiableList(validationItems);
    }

    void addEvaluationItem(EvaluationItem annotation) {
        this.evaluationItems.add(annotation);
        this.validationItems.add(annotation);
    }

    void truncateAnnotationsToSize(int size) {
        evaluationItems.subList(size, evaluationItems.size()).clear();
    }

    private Optional<Schema> resolveExternalSchema(String uri) {
        String baseUri = UriUtil.getUriWithoutFragment(uri);
        if (schemaRegistry.get(baseUri) != null) {
            return Optional.empty();
        }
        return schemaResolver.resolve(baseUri)
                .toJsonNode(jsonNodeFactory)
                .flatMap(node -> {
                    try {
                        jsonParser.parseRootSchema(URI.create(baseUri), node);
                        return resolveSchema(uri);
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                });
    }
}
