package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

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
    private final Deque<URI> dynamicScope = new ArrayDeque<>();
    private final Deque<RefStackItem> refStack = new ArrayDeque<>();
    private final LinkedList<String> evaluationStack = new LinkedList<>();
    private final AnnotationTree annotationTree = new AnnotationTree();
    private final List<Error> errors = new ArrayList<>();

    EvaluationContext(JsonNodeFactory jsonNodeFactory,
                      JsonParser jsonParser,
                      SchemaRegistry schemaRegistry,
                      SchemaResolver schemaResolver) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.jsonParser = Objects.requireNonNull(jsonParser);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
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
        Schema schema = resolveSchema(compoundUri);
        if (schema == null) {
            throw new SchemaNotFoundException(compoundUri);
        }
        return validateAgainstRefSchema(schema, node);
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
        Schema schema = resolveDynamicSchema(compoundUri);
        if (schema == null) {
            throw new SchemaNotFoundException(compoundUri);
        }
        return validateAgainstRefSchema(schema, node);
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
        Schema schema = resolveRecursiveSchema();
        if (schema == null) {
            throw new SchemaNotFoundException(CompoundUri.fromString(schemaRef));
        }
        return validateAgainstRefSchema(schema, node);
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
        Schema schema = schemaRegistry.get(compoundUri);
        if (schema == null) {
            throw new SchemaNotFoundException(compoundUri);
        }
        return validateAgainstSchema(schema, node);
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

    Set<String> calculateEvaluatedInstancesFromParent() {
        /* As on evaluationStack there are no paths to schemas in arrays (e.g. "/items/0")
         * this needs to be accounted for with correctedParentPath */
        String parentPath = evaluationStack.get(1);
        String correctedParentPath = UriUtil.getJsonPointerParent(evaluationStack.element());

        List<Annotation> annotations = annotationTree.getNode(parentPath).toList();
        Set<String> all = new HashSet<>(annotations.size() + errors.size());
        for (Annotation annotation : annotations) {
            if (annotation.getEvaluationPath().startsWith(correctedParentPath)) {
                all.add(annotation.getInstanceLocation());
            }
        }
        for (Error error : errors) {
            if (error.getEvaluationPath().startsWith(correctedParentPath)) {
                all.add(error.getInstanceLocation());
            }
        }
        return all;
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

        boolean valid = true;
        for (EvaluatorWrapper evaluator : schema.getEvaluators()) {
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
                valid = false;
                errors.add(new Error(evaluationPath, schema.getSchemaLocation(), node.getJsonPointer(), evaluator.getKeyword(), result.getError()));
            }
            evaluationStack.pop();
        }
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

    private Schema resolveSchema(CompoundUri compoundUri) {
        CompoundUri resolvedUri = UriUtil.resolveUri(dynamicScope.element(), compoundUri);
        Schema schema = schemaRegistry.get(resolvedUri);
        if (schema != null) {
            return schema;
        }
        schema = schemaRegistry.getDynamic(resolvedUri);
        if (schema != null) {
            return schema;
        }
        return resolveExternalSchema(compoundUri, resolvedUri);
    }

    private Schema resolveDynamicSchema(CompoundUri compoundUri) {
        CompoundUri resolvedUri = UriUtil.resolveUri(dynamicScope.element(), compoundUri);
        Schema staticSchema = schemaRegistry.get(resolvedUri);
        if (staticSchema != null) {
            return staticSchema;
        }

        Iterator<URI> it = dynamicScope.descendingIterator();
        while (it.hasNext()) {
            Schema schema = schemaRegistry.getDynamic(new CompoundUri(it.next(), resolvedUri.fragment));
            if (schema != null) {
                return schema;
            }
        }
        return null;
    }

    private Schema resolveRecursiveSchema() {
        Schema schema = schemaRegistry.get(dynamicScope.element());
        for (URI uri : dynamicScope) {
            Schema recursedSchema = schemaRegistry.getDynamic(uri);
            if (recursedSchema == null) {
                return schema;
            } else {
                schema = recursedSchema;
            }
        }
        return schema;
    }

    private String resolveEvaluationPath(EvaluatorWrapper evaluator) {
        if (refStack.isEmpty()) {
            return evaluator.getKeywordPath();
        }
        RefStackItem refItem = refStack.peek();
        String evaluationPathPart = evaluator.getKeywordPath().substring(refItem.schemaLocation.length());
        return refItem.evaluationPath + evaluationPathPart;
    }

    private Schema resolveExternalSchema(CompoundUri originalRef, CompoundUri resolvedUri) {
        if (schemaRegistry.get(resolvedUri.uri) != null) {
            return null;
        }
        return schemaResolver.resolve(resolvedUri.uri.toString())
                .toJsonNode(jsonNodeFactory)
                .map(node -> {
                    jsonParser.parseRootSchema(resolvedUri.uri, node);
                    return resolveSchema(originalRef);
                }).orElse(null);
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
