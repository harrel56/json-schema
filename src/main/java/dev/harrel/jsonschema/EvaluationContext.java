package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;

/**
 * {@code EvaluationContext} class represents state of current evaluation (instance validation against schema).
 * {@link Evaluator} can use this class for its processing logic.
 *
 * @see Evaluator
 */
public final class EvaluationContext {
    private final JsonNodeFactory schemaNodeFactory;
    private final JsonNodeFactory instanceNodeFactory;
    private final JsonParser jsonParser;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;
    private final Deque<URI> dynamicScope = new ArrayDeque<>();
    private final Deque<RefStackItem> refStack = new ArrayDeque<>();
    private final Deque<Integer> annotationsBeforeStack = new ArrayDeque<>();
    private final Deque<Map<String, Annotation>> siblingAnnotationsStack = new ArrayDeque<>();
    private final Deque<String> evaluationStack = new ArrayDeque<>();
    private final List<Annotation> annotations = new ArrayList<>();
    private final List<LazyError> errors = new ArrayList<>();

    EvaluationContext(JsonNodeFactory schemaNodeFactory,
                      JsonNodeFactory instanceNodeFactory,
                      JsonParser jsonParser,
                      SchemaRegistry schemaRegistry,
                      SchemaResolver schemaResolver) {
        this.schemaNodeFactory = Objects.requireNonNull(schemaNodeFactory);
        this.instanceNodeFactory = Objects.requireNonNull(instanceNodeFactory);
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

    JsonNodeFactory getInstanceNodeFactory() {
        return instanceNodeFactory;
    }

    List<Annotation> getAnnotations() {
        return unmodifiableList(annotations);
    }

    List<LazyError> getErrors() {
        return unmodifiableList(errors);
    }

    Object getSiblingAnnotation(String sibling) {
        Annotation annotation = siblingAnnotationsStack.element().get(sibling);
        return annotation == null ? null : annotation.getAnnotation();
    }

    @SuppressWarnings("unchecked")
    Map.Entry<Integer, Set<Integer>> calculateEvaluatedItems(String instanceLocation) {
        int fromIdx = annotationsBeforeStack.element();
        Set<Integer> items = new HashSet<>();
        int maxIdx = 0;
        for (int i = fromIdx; i < annotations.size(); i++) {
            Annotation annotation = annotations.get(i);
            if (annotation.getInstanceLocation().equals(instanceLocation)) {
                if (Keyword.ITEM_KEYWORDS.contains(annotation.getKeyword())) {
                    if (annotation.getAnnotation() instanceof Boolean) {
                        return new AbstractMap.SimpleEntry<>(Integer.MAX_VALUE, emptySet());
                    }
                    maxIdx = Math.max(maxIdx, (Integer) annotation.getAnnotation());
                } else if (annotation.getKeyword().equals(Keyword.CONTAINS)) {
                    items.addAll((Collection<Integer>) annotation.getAnnotation());
                }
            }
        }
        return new AbstractMap.SimpleEntry<>(maxIdx, items);
    }

    @SuppressWarnings("unchecked")
    Set<String> calculateEvaluatedProperties(String instanceLocation) {
        int fromIdx = annotationsBeforeStack.element();
        Set<String> props = new HashSet<>();
        for (int i = fromIdx; i < annotations.size(); i++) {
            Annotation annotation = annotations.get(i);
            if (annotation.getInstanceLocation().equals(instanceLocation) && Keyword.PROPERTY_KEYWORDS.contains(annotation.getKeyword())) {
                props.addAll((Collection<String>) annotation.getAnnotation());
            }
        }
        return props;
    }

    boolean validateAgainstSchema(Schema schema, JsonNode node) {
        boolean outOfDynamicScope = !schema.getParentUri().equals(dynamicScope.peek());
        if (outOfDynamicScope) {
            dynamicScope.push(schema.getParentUri());
        }

        int annotationsBefore = annotations.size();
        Map<String, Annotation> siblingAnnotations = new HashMap<>();
        annotationsBeforeStack.push(annotationsBefore);
        siblingAnnotationsStack.push(siblingAnnotations);

        List<EvaluatorWrapper> evaluators = schema.getEvaluators();
        int evaluatorsSize = evaluators.size();
        boolean valid = true;
        for (int i = 0; i < evaluatorsSize; i++) {
            EvaluatorWrapper evaluator = evaluators.get(i);
            String evaluationPath = resolveEvaluationPath(evaluator);
            evaluationStack.push(evaluationPath);
            int errorsBefore = errors.size();
            Evaluator.Result result = evaluator.evaluate(this, node);
            if (result.getAnnotation() != null) {
                Annotation annotation = new Annotation(evaluationPath, schema.getSchemaLocation(), node.getJsonPointer(), evaluator.getKeyword(), result.getAnnotation());
                siblingAnnotations.put(evaluator.getKeyword(), annotation);
                annotations.add(annotation);
            }
            if (result.isValid()) {
                /* Discarding errors that were produced by keywords evaluated to true */
                errors.subList(errorsBefore, errors.size()).clear();
            } else {
                valid = false;
                errors.add(new LazyError(evaluationPath, schema.getSchemaLocation(), node.getJsonPointer(), evaluator.getKeyword(), result.getErrorSupplier()));
            }
            evaluationStack.pop();
        }
        if (!valid) {
            /* Discarding annotations */
            annotations.subList(annotationsBefore, annotations.size()).clear();
        }
        siblingAnnotationsStack.pop();
        annotationsBeforeStack.pop();
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
        Schema schema = schemaRegistry.get(compoundUri);
        if (schema != null) {
            return schema;
        }
        schema = schemaRegistry.getDynamic(compoundUri);
        if (schema != null) {
            return schema;
        }
        return resolveExternalSchema(compoundUri);
    }

    private Schema resolveDynamicSchema(CompoundUri compoundUri) {
        Schema staticSchema = schemaRegistry.get(compoundUri);
        if (staticSchema != null) {
            return staticSchema;
        }

        Iterator<URI> it = dynamicScope.descendingIterator();
        while (it.hasNext()) {
            Schema schema = schemaRegistry.getDynamic(new CompoundUri(it.next(), compoundUri.fragment));
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

    private Schema resolveExternalSchema(CompoundUri compoundUri) {
        if (schemaRegistry.get(compoundUri.uri) != null) {
            return null;
        }
        return schemaResolver.resolve(compoundUri.uri.toString())
                .toJsonNode(schemaNodeFactory)
                .map(node -> {
                    jsonParser.parseRootSchema(compoundUri.uri, node);
                    return resolveSchema(compoundUri);
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
