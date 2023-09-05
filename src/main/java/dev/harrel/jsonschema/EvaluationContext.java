package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

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
    private final Set<String> activeVocabularies;
    private final Deque<URI> dynamicScope = new LinkedList<>();
    private final Deque<RefStackItem> refStack = new LinkedList<>();
    private final Deque<String> evaluationStack = new LinkedList<>();
    private final List<Annotation> annotations = new ArrayList<>();
    private final List<Error> errors = new ArrayList<>();

    EvaluationContext(JsonNodeFactory jsonNodeFactory,
                      JsonParser jsonParser,
                      SchemaRegistry schemaRegistry,
                      SchemaResolver schemaResolver,
                      Set<String> activeVocabularies) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.jsonParser = Objects.requireNonNull(jsonParser);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
        this.activeVocabularies = Objects.requireNonNull(activeVocabularies);
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

    List<Error> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    <T> Optional<T> getSiblingAnnotation(String sibling, Class<T> annotationType) {
        String parentPath = UriUtil.getJsonPointerParent(evaluationStack.element());
        return annotations.stream()
                .filter(item -> sibling.equals(item.getKeyword()))
                .filter(item -> parentPath.equals(UriUtil.getJsonPointerParent(item.getEvaluationPath())))
                .map(Annotation::getAnnotation)
                .filter(annotationType::isInstance)
                .map(annotationType::cast)
                .findAny();
    }

    /**
     * Returns collected annotations up to this point.
     * Discarded annotations are not included.
     *
     * @return unmodifiable list of annotations
     */
    List<Annotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    boolean validateAgainstSchema(Schema schema, JsonNode node) {
        boolean outOfDynamicScope = isOutOfDynamicScope(schema.getParentUri());
        if (outOfDynamicScope) {
            dynamicScope.push(schema.getParentUri());
        }

        int annotationsBefore = annotations.size();
        boolean valid = true;
        List<EvaluatorWrapper> filteredEvaluators = schema.getEvaluators().stream()
                .filter(ev -> ev.getVocabularies().stream().anyMatch(activeVocabularies::contains) || ev.getVocabularies().isEmpty())
                .collect(Collectors.toList());
        for (EvaluatorWrapper evaluator : filteredEvaluators) {
            String evaluationPath = resolveEvaluationPath(evaluator);
            evaluationStack.push(evaluationPath);
            int errorsBefore = errors.size();
            Evaluator.Result result = evaluator.evaluate(this, node);
            if (result.isValid()) {
                /* Discarding valid sub-schema errors */
                errors.subList(errorsBefore, errors.size()).clear();
                annotations.add(new Annotation(evaluationPath, schema.getSchemaLocation(), node.getJsonPointer(), evaluator.getKeyword(), result.getAnnotation()));
            } else {
                errors.add(new Error(evaluationPath, schema.getSchemaLocation(), node.getJsonPointer(), evaluator.getKeyword(), result.getError()));
            }
            valid = valid && result.isValid();
            evaluationStack.pop();
        }
        if (!valid) {
            /* Discarding annotations */
            annotations.subList(annotationsBefore, annotations.size()).clear();
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
        String resolvedUri = UriUtil.resolveUri(dynamicScope.element(), ref);
        return OptionalUtil.firstPresent(
                () -> Optional.ofNullable(schemaRegistry.get(resolvedUri)),
                () -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri)),
                () -> resolveExternalSchema(resolvedUri)
        );
    }

    private Optional<Schema> resolveDynamicSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(dynamicScope.element(), ref);
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
        if (!currentPath.startsWith(refItem.schemaLocation)) {
            throw new IllegalStateException("Unexpected evaluation path resolution error");
        }

        String evaluationPathPart = currentPath.substring(refItem.schemaLocation.length());
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
                    jsonParser.parseRootSchema(URI.create(baseUri), node);
                    return resolveSchema(uri);
                });
    }

    private static class RefStackItem {
        private final String schemaLocation;
        private final String evaluationPath;

        public RefStackItem(String schemaLocation, String evaluationPath) {
            this.schemaLocation = schemaLocation;
            this.evaluationPath = evaluationPath;
        }
    }
}
