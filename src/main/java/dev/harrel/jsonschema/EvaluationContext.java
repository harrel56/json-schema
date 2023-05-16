package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

/**
 * {@code EvaluationContext} class represents state of current evaluation (instance validation against schema).
 * {@link Evaluator} can use this class for its processing logic.
 *
 * @see Evaluator
 */
public final class EvaluationContext {
    private final JsonNodeFactory jsonNodeFactory;
    private final JsonParser jsonParser;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;
    private final Deque<URI> dynamicScope = new LinkedList<>();
    private final Deque<RefStackItem> refStack = new LinkedList<>();
    private final Deque<String> evaluationStack = new LinkedList<>();
    private final List<EvaluationItem> evaluationItems = new ArrayList<>();
    private final List<EvaluationItem> validationItems = new ArrayList<>();

    EvaluationContext(JsonNodeFactory jsonNodeFactory,
                      JsonParser jsonParser,
                      SchemaRegistry schemaRegistry,
                      SchemaResolver schemaResolver) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.jsonParser = Objects.requireNonNull(jsonParser);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
    }

    /**
     * Returns collected annotations up to this point.
     * Discarded annotations are not included.
     *
     * @return unmodifiable list of annotations
     */
    public List<EvaluationItem> getEvaluationItems() {
        return Collections.unmodifiableList(evaluationItems);
    }

    /**
     * Resolves schema using provided reference string, and then validates instance node against it.
     * This method can invoke {@link SchemaResolver}.
     *
     * @param schemaRef reference to the schema
     * @param node      instance node to be validated
     * @return if validation was successful
     * @throws SchemaNotFoundException when schema cannot be resolved
     */
    public boolean resolveRefAndValidate(String schemaRef, JsonNode node) {
        return resolveSchema(schemaRef)
                .map(schema -> validateAgainstRefSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(schemaRef));
    }

    /**
     * Dynamically resolves schema using provided reference string, and then validates instance node against it.
     * This method is specifically created for <i>$dynamicRef</i> keyword.
     *
     * @param schemaRef reference to the schema
     * @param node      instance node to be validated
     * @return if validation was successful
     * @throws SchemaNotFoundException when schema cannot be resolved
     */
    public boolean resolveDynamicRefAndValidate(String schemaRef, JsonNode node) {
        return resolveDynamicSchema(schemaRef)
                .map(schema -> validateAgainstRefSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(schemaRef));
    }

    /**
     * Resolves <i>internal</i> schema using provided reference string and then validates instance node against it.
     * This method should only be used for internal schema resolutions, that means schema/evaluator calling this
     * method should only refer to schema instances which are descendants of calling node.
     * Note that this method is semantically different from {@link EvaluationContext#resolveRefAndValidate} and it
     * cannot invoke {@link SchemaResolver}.
     *
     * @param schemaRef reference to the schema
     * @param node      instance node to be validated
     * @return if validation was successful
     * @throws SchemaNotFoundException when schema cannot be resolved
     */
    public boolean resolveInternalRefAndValidate(String schemaRef, JsonNode node) {
        return Optional.ofNullable(schemaRegistry.get(schemaRef))
                .map(schema -> validateAgainstSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(schemaRef));
    }

    List<EvaluationItem> getValidationItems() {
        return Collections.unmodifiableList(validationItems);
    }

    boolean validateAgainstSchema(Schema schema, JsonNode node) {
        boolean outOfDynamicScope = isOutOfDynamicScope(schema.getParentUri());
        if (outOfDynamicScope) {
            dynamicScope.push(schema.getParentUri());
        }

        int annotationsBefore = getEvaluationItems().size();
        boolean valid = true;
        for (EvaluatorWrapper evaluator : schema.getEvaluators()) {
            String evaluationPath = resolveEvaluationPath(evaluator);
            evaluationStack.push(evaluationPath);
            Evaluator.Result result = evaluator.evaluate(this, node);
            EvaluationItem evaluationItem = new EvaluationItem(
                    evaluationPath, schema.getSchemaLocation(), node.getJsonPointer(),
                    evaluator.getKeyword(), result.isValid(), result.getAnnotation(), result.getError());
            evaluationItems.add(evaluationItem);
            validationItems.add(evaluationItem);
            valid = valid && result.isValid();
            evaluationStack.pop();
        }
        if (!valid) {
            /* Discarding annotations */
            evaluationItems.subList(annotationsBefore, evaluationItems.size()).clear();
        }
        if (outOfDynamicScope) {
            dynamicScope.pop();
        }
        return valid;
    }

    private boolean validateAgainstRefSchema(Schema schema, JsonNode node) {
        refStack.push(new RefStackItem(UriUtil.getJsonPointer(schema.getSchemaLocation()), evaluationStack.peek()));
        boolean valid = validateAgainstSchema(schema, node);
        refStack.pop();
        return valid;
    }

    private Optional<Schema> resolveSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(dynamicScope.peek(), ref);
        return Optional.ofNullable(schemaRegistry.get(resolvedUri))
                .or(() -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri)))
                .or(() -> resolveExternalSchema(resolvedUri));
    }

    private Optional<Schema> resolveDynamicSchema(String ref) {
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

    private boolean isOutOfDynamicScope(URI uri) {
        return dynamicScope.isEmpty() || !uri.equals(dynamicScope.peek());
    }

    private String resolveEvaluationPath(EvaluatorWrapper evaluator) {
        if (refStack.isEmpty()) {
            return evaluator.getKeywordPath();
        }
        RefStackItem refItem = refStack.peek();
        String currentPath = evaluator.getKeywordPath();
        if (!currentPath.startsWith(refItem.schemaLocation())) {
            throw new IllegalStateException("Unexpected evaluation path resolution error");
        }

        String evaluationPathPart = currentPath.substring(refItem.schemaLocation().length());
        return refItem.evaluationPath + evaluationPathPart;
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

    private record RefStackItem(String schemaLocation, String evaluationPath) {}
}
