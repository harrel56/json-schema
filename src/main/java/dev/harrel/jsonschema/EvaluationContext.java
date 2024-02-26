package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
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
    private final boolean disabledSchemaValidation;
    private final Deque<URI> dynamicScope = new ArrayDeque<>();
    private final Deque<RefStackItem> refStack = new ArrayDeque<>();
    private final LinkedList<String> evaluationStack = new LinkedList<>();
    private final AnnotationTree annotationTree = new AnnotationTree();
    private final List<Error> errors = new ArrayList<>();

    EvaluationContext(JsonNodeFactory jsonNodeFactory,
                      JsonParser jsonParser,
                      SchemaRegistry schemaRegistry,
                      SchemaResolver schemaResolver,
                      Set<String> activeVocabularies,
                      boolean disabledSchemaValidation) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.jsonParser = Objects.requireNonNull(jsonParser);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
        this.activeVocabularies = Objects.requireNonNull(activeVocabularies);
        this.disabledSchemaValidation = disabledSchemaValidation;
        this.evaluationStack.push("");
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
        return resolveRefAndValidate(CompoundUri.fromString(schemaRef), node);
    }

    boolean resolveRefAndValidate(CompoundUri compoundUri, JsonNode node) {
        return resolveSchema(compoundUri)
                .map(schema -> validateAgainstRefSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(compoundUri));
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
        return resolveDynamicRefAndValidate(CompoundUri.fromString(schemaRef), node);
    }

    boolean resolveDynamicRefAndValidate(CompoundUri compoundUri, JsonNode node) {
        return resolveDynamicSchema(compoundUri)
                .map(schema -> validateAgainstRefSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(compoundUri));
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
                .orElseThrow(() -> new SchemaNotFoundException(CompoundUri.fromString(schemaRef)));
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
        return resolveInternalRefAndValidate(CompoundUri.fromString(schemaRef), node);
    }

    boolean resolveInternalRefAndValidate(CompoundUri compoundUri, JsonNode node) {
        return Optional.ofNullable(schemaRegistry.get(compoundUri))
                .map(schema -> validateAgainstSchema(schema, node))
                .orElseThrow(() -> new SchemaNotFoundException(compoundUri));
    }

    List<Error> getErrors() {
        return unmodifiableList(errors);
    }

    <T> Optional<T> getSiblingAnnotation(String sibling, String instanceLocation, Class<T> annotationType) {
        return getSiblingAnnotation(sibling, instanceLocation)
                .filter(annotationType::isInstance)
                .map(annotationType::cast);
    }

    Optional<Object> getSiblingAnnotation(String sibling, String instanceLocation) {
        for (Annotation annotation : annotationTree.getNode(evaluationStack.get(1)).annotations) {
            if (instanceLocation.equals(annotation.getInstanceLocation()) && sibling.equals(annotation.getKeyword())) {
                return Optional.of(annotation.getAnnotation());
            }
        }
        return Optional.empty();
    }

    AnnotationTree getAnnotationTree() {
        return annotationTree;
    }

    Stream<Annotation> getAnnotationsFromParent() {
        /* As on evaluationStack there are no paths to schemas in arrays (e.g. "/items/0")
        * this needs to be accounted for with correctedParentPath */
        String parentPath = evaluationStack.get(1);
        String correctedParentPath = UriUtil.getJsonPointerParent(evaluationStack.element());
        return annotationTree.getNode(parentPath).stream().filter(item -> item.getEvaluationPath().startsWith(correctedParentPath));
    }

    boolean validateAgainstSchema(Schema schema, JsonNode node) {
        boolean outOfDynamicScope = !schema.getParentUri().equals(dynamicScope.peek());
        if (outOfDynamicScope) {
            dynamicScope.push(schema.getParentUri());
        }

        String parentSchemaLocation = evaluationStack.size() > 1 ? evaluationStack.get(1) : null;
        AnnotationTree.Node treeNode = annotationTree.createIfAbsent(parentSchemaLocation, evaluationStack.element());
        int nodesBefore = treeNode.nodes.size();
        int annotationsBefore = treeNode.annotations.size();
        Stream<EvaluatorWrapper> evaluatorStream = schema.getEvaluators().stream();
        if (!disabledSchemaValidation) {
            evaluatorStream = evaluatorStream.filter(ev -> ev.getVocabularies().stream().anyMatch(activeVocabularies::contains) || ev.getVocabularies().isEmpty());
        }

        boolean valid = evaluatorStream.reduce(true, (validAcc, evaluator) -> {
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
                () -> Optional.ofNullable(schemaRegistry.get(resolvedUri)),
                () -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri)),
                () -> resolveExternalSchema(compoundUri, resolvedUri)
        );
    }

    private Optional<Schema> resolveDynamicSchema(CompoundUri compoundUri) {
        CompoundUri resolvedUri = UriUtil.resolveUri(dynamicScope.element(), compoundUri);
        Schema staticSchema = schemaRegistry.get(resolvedUri);
        if (staticSchema != null) {
            return Optional.of(staticSchema);
        }

        Iterator<URI> it = dynamicScope.descendingIterator();
        while (it.hasNext()) {
            Schema schema = schemaRegistry.getDynamic(new CompoundUri(it.next(), resolvedUri.fragment));
            if (schema != null) {
                return Optional.of(schema);
            }
        }
        return Optional.empty();
    }

    private Optional<Schema> resolveRecursiveSchema() {
        Schema schema = schemaRegistry.get(dynamicScope.element());
        for (URI uri : dynamicScope) {
            Schema recursedSchema = schemaRegistry.getDynamic(uri);
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

        private RefStackItem(String schemaLocation, String evaluationPath) {
            this.schemaLocation = schemaLocation;
            this.evaluationPath = evaluationPath;
        }
    }
}
