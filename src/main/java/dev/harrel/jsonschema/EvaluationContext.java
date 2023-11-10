package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;

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
    private final LinkedList<String> evaluationStack = new LinkedList<>();
    private final LinkedList<String> schemaStack = new LinkedList<>();
    private final AnnotationTree annotationTree = new AnnotationTree();
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
        URI refUri = UriUtil.getUriWithoutFragment(schemaRef);
        String refFragment = UriUtil.getJsonPointer(schemaRef);
        return resolveRefAndValidate(new CompoundUri(refUri, refFragment), node);
    }

    boolean resolveRefAndValidate(CompoundUri compoundUri, JsonNode node) {
        return resolveSchema(compoundUri)
                .map(schema -> validateAgainstRefSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(compoundUri.uri, compoundUri.fragment));
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
        URI refUri = UriUtil.getUriWithoutFragment(schemaRef);
        String refFragment = UriUtil.getJsonPointer(schemaRef);
        return resolveDynamicRefAndValidate(new CompoundUri(refUri, refFragment), node);
    }

    boolean resolveDynamicRefAndValidate(CompoundUri compoundUri, JsonNode node) {
        return resolveDynamicSchema(compoundUri)
                .map(schema -> validateAgainstRefSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(compoundUri.uri, compoundUri.fragment));
    }

    /**
     * Recursively resolves schema using provided reference string (current implementation behaves the same for any reference string),
     * and then validates instance node against it.
     * This method is specifically created for <i>$recursiveRef</i> keyword.
     *
     * @param schemaRef reference to the schema (specification-wise this should always have a value of '#')
     * @param node      instance node to be validated
     * @return if validation was successful
     * @throws SchemaNotFoundException when schema cannot be resolved
     */
    public boolean resolveRecursiveRefAndValidate(String schemaRef, JsonNode node) {
        return resolveRecursiveSchema()
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
        URI refUri = UriUtil.getUriWithoutFragment(schemaRef);
        String refFragment = UriUtil.getJsonPointer(schemaRef);
        return resolveInternalRefAndValidate(refUri, refFragment, node);
    }

    boolean resolveInternalRefAndValidate(CompoundUri compoundUri, JsonNode node) {
        return resolveInternalRefAndValidate(compoundUri.uri, compoundUri.fragment, node);
    }

    boolean resolveInternalRefAndValidate(URI refUri, String refFragment, JsonNode node) {
        return Optional.ofNullable(schemaRegistry.get(refUri, refFragment))
                .map(schema -> validateAgainstSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(refUri, refFragment));
    }

    List<Error> getErrors() {
        return unmodifiableList(errors);
    }

    <T> Optional<T> getSiblingAnnotation(String sibling, Class<T> annotationType) {
        return getSiblingAnnotation(sibling)
                .filter(annotationType::isInstance)
                .map(annotationType::cast);
    }

    Optional<Object> getSiblingAnnotation(String sibling) {
        String parentPath = UriUtil.getJsonPointerParent(evaluationStack.element());
        return annotationTree.getNode(schemaStack.element()).annotations.stream()
                .filter(item -> sibling.equals(item.getKeyword()))
                .filter(item -> item.getEvaluationPath().startsWith(parentPath))
                .filter(item -> !item.getEvaluationPath().substring(parentPath.length() + 1).contains("/"))
                .map(Annotation::getAnnotation)
                .findFirst();
    }

    /**
     * Returns collected annotations up to this point.
     * Discarded annotations are not included.
     *
     * @return unmodifiable list of annotations
     */
    List<Annotation> getAnnotations() {
        return unmodifiableList(annotationTree.getAllAnnotations().collect(Collectors.toList()));
    }

    Stream<Annotation> getAnnotationsFromParent(String parentPath) {
        return annotationTree.getNode(parentPath).stream();
    }

    boolean validateAgainstSchema(Schema schema, JsonNode node) {
        String parentSchemaLocation = schemaStack.peek();
        schemaStack.push(schema.getSchemaLocationFragment());
        boolean outOfDynamicScope = !schema.getParentUri().equals(dynamicScope.peek());
        if (outOfDynamicScope) {
            dynamicScope.push(schema.getParentUri());
        }

        String schemaLocation = schemaStack.element();
        AnnotationTree.Node treeNode = annotationTree.createIfAbsent(parentSchemaLocation, schemaLocation);
        int nodesBefore = treeNode.nodes.size();
        int annotationsBefore = treeNode.annotations.size();
        boolean valid = schema.getEvaluators().stream()
                .filter(ev -> ev.getVocabularies().stream().anyMatch(activeVocabularies::contains) || ev.getVocabularies().isEmpty())
                .reduce(true, (validAcc, evaluator) -> {
                    String evaluationPath = resolveEvaluationPath(evaluator);
                    evaluationStack.push(evaluationPath);
                    int errorsBefore = errors.size();
                    Evaluator.Result result = evaluator.evaluate(this, node);
                    if (result.isValid()) {
                        /* Discarding errors that were produced by keywords evaluated to true */
                        errors.subList(errorsBefore, errors.size()).clear();
                        Annotation annotation = new Annotation(evaluationPath, schema.getSchemaLocation(), node.getJsonPointer(), evaluator.getKeyword(), result.getAnnotation());
                        treeNode.annotations.add(annotation);
                    } else {
                        errors.add(new Error(evaluationPath, schema.getSchemaLocation(), node.getJsonPointer(), evaluator.getKeyword(), result.getError()));
                    }
                    evaluationStack.pop();
                    return validAcc && result.isValid();
                }, (v1, v2) -> v1 && v2);
        if (!valid) {
            /* Discarding annotations */
            treeNode.nodes.subList(nodesBefore, treeNode.nodes.size()).clear();
            treeNode.annotations.subList(annotationsBefore, treeNode.annotations.size()).clear();
        }
        if (outOfDynamicScope) {
            dynamicScope.pop();
        }
        schemaStack.pop();
        return valid;
    }

    private boolean validateAgainstRefSchema(Schema schema, JsonNode node) {
        refStack.push(new RefStackItem(schema.getSchemaLocationFragment(), evaluationStack.peek()));
        boolean valid = validateAgainstSchema(schema, node);
        refStack.pop();
        return valid;
    }

    private Optional<Schema> resolveSchema(CompoundUri compoundUri) {
        CompoundUri resolvedUri = UriUtil.resolveUri(dynamicScope.element(), compoundUri);
        return OptionalUtil.firstPresent(
                () -> Optional.ofNullable(schemaRegistry.get(resolvedUri.uri, resolvedUri.fragment)),
                () -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri.uri, resolvedUri.fragment)),
                () -> resolveExternalSchema(compoundUri, resolvedUri)
        );
    }

    private Optional<Schema> resolveDynamicSchema(CompoundUri compoundUri) {
        CompoundUri resolvedUri = UriUtil.resolveUri(dynamicScope.element(), compoundUri);
        Schema staticSchema = schemaRegistry.get(resolvedUri.uri, resolvedUri.fragment);
        if (staticSchema != null) {
            return Optional.of(staticSchema);
        }

        Iterator<URI> it = dynamicScope.descendingIterator();
        while (it.hasNext()) {
            Schema schema = schemaRegistry.getDynamic(it.next(), resolvedUri.fragment);
            if (schema != null) {
                return Optional.of(schema);
            }
        }
        return Optional.empty();
    }

    private Optional<Schema> resolveRecursiveSchema() {
        Schema schema = schemaRegistry.get(dynamicScope.element().toString());
        for (URI uri : dynamicScope) {
            Schema recursedSchema = schemaRegistry.getDynamic(uri.toString());
            if (recursedSchema == null) {
                return Optional.of(schema);
            } else {
                schema = recursedSchema;
            }
        }
        return Optional.of(schema);
    }

    private String resolveEvaluationPath(EvaluatorWrapper evaluator) {
        if (refStack.isEmpty()) {
            return evaluator.getKeywordPath();
        }
        RefStackItem refItem = refStack.peek();
        String evaluationPathPart = evaluator.getKeywordPath().substring(refItem.schemaLocation.length());
        return refItem.evaluationPath + evaluationPathPart;
    }

    private Optional<Schema> resolveExternalSchema(CompoundUri originalRef, CompoundUri resolvedUri) {
        if (schemaRegistry.get(resolvedUri.uri) != null) {
            return Optional.empty();
        }
        return schemaResolver.resolve(resolvedUri.uri.toString())
                .toJsonNode(jsonNodeFactory)
                .flatMap(node -> {
                    jsonParser.parseRootSchema(resolvedUri.uri, node);
                    return resolveSchema(originalRef);
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
