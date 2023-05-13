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
     * Resolves schema using provided reference string, and then validates instance node against it.
     * This method can invoke {@link SchemaResolver}.
     * @param schemaRef reference to the schema
     * @param node instance node to be validated
     * @return if validation was successful
     * @throws IllegalStateException when schema cannot be resolved
     */
    public boolean validateAgainstSchema(String schemaRef, JsonNode node) {
        return resolveSchema(schemaRef)
                .map(schema -> validateAgainstSchema(schema, node))
                .orElseThrow(() -> new IllegalStateException("Resolution of schema [%s] failed and was required".formatted(schemaRef)));
    }

    boolean validateAgainstRequiredSchema(String schemaRef, JsonNode node) {
        return validateAgainstSchema(resolveRequiredSchema(schemaRef), node);
    }

    boolean validateAgainstSchema(Schema schema, JsonNode node) {
        boolean outOfDynamicScope = isOutOfDynamicScope(schema.getParentUri());
        if (outOfDynamicScope) {
            dynamicScope.push(schema.getParentUri());
        }

        int annotationsBefore = getEvaluationItems().size();
        boolean valid = true;
        for (EvaluatorWrapper evaluator : schema.getEvaluators()) {
            Evaluator.Result result = evaluator.evaluate(this, node);
            EvaluationItem evaluationItem = new EvaluationItem(
                    evaluator.getKeywordPath(), schema.getSchemaLocation(), node.getJsonPointer(),
                    evaluator.getKeyword(), result.isValid(), result.getAnnotation(), result.getError());
            evaluationItems.add(evaluationItem);
            validationItems.add(evaluationItem);
            valid = valid && result.isValid();
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

    Optional<Schema> resolveSchema(String ref) {
        String resolvedUri = UriUtil.resolveUri(dynamicScope.peek(), ref);
        return Optional.ofNullable(schemaRegistry.get(resolvedUri))
                .or(() -> Optional.ofNullable(schemaRegistry.getDynamic(resolvedUri)))
                .or(() -> resolveExternalSchema(resolvedUri));
    }

    Optional<Schema> resolveDynamicSchema(String ref) {
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

    Schema resolveRequiredSchema(String ref) {
        return Optional.ofNullable(schemaRegistry.get(ref))
                .orElseThrow(() -> new IllegalStateException("Resolution of schema [%s] failed and was required".formatted(ref)));
    }

    List<EvaluationItem> getValidationItems() {
        return Collections.unmodifiableList(validationItems);
    }

    private boolean isOutOfDynamicScope(URI uri) {
        return dynamicScope.isEmpty() || !uri.equals(dynamicScope.peek());
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
