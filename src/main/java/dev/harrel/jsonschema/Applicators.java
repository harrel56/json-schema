package dev.harrel.jsonschema;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.harrel.jsonschema.Vocabulary.APPLICATOR_VOCABULARY;
import static dev.harrel.jsonschema.Vocabulary.UNEVALUATED_VOCABULARY;
import static java.util.Collections.*;

class PrefixItemsEvaluator implements Evaluator {
    private final List<String> prefixRefs;

    PrefixItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.prefixRefs = unmodifiableList(node.asArray().stream()
                .map(ctx::getAbsoluteUri)
                .collect(Collectors.toList()));
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        List<JsonNode> elements = node.asArray();
        int size = Math.min(prefixRefs.size(), elements.size());
        boolean valid = IntStream.range(0, size)
                .boxed()
                .filter(idx -> ctx.resolveInternalRefAndValidate(prefixRefs.get(idx), elements.get(idx)))
                .count() == size;
        return valid ? Result.success(prefixRefs.size()) : Result.failure();
    }
}

class ItemsEvaluator implements Evaluator {
    private final String schemaRef;

    ItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getAbsoluteUri(node);
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }
        List<JsonNode> array = node.asArray();
        int prefixItemsSize = ctx.getSiblingAnnotation(Keyword.PREFIX_ITEMS, Integer.class).orElse(0);
        int size = Math.max(array.size() - prefixItemsSize, 0);
        boolean valid = array.stream()
                .skip(prefixItemsSize)
                .filter(element -> ctx.resolveInternalRefAndValidate(schemaRef, element))
                .count() == size;
        return valid ? Result.success(true) : Result.failure();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class ContainsEvaluator implements Evaluator {
    private final String schemaRef;
    private final boolean minContainsZero;

    ContainsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getAbsoluteUri(node);
        this.minContainsZero = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.MIN_CONTAINS))
                .map(JsonNode::asInteger)
                .map(BigInteger::intValueExact)
                .map(min -> min == 0)
                .orElse(false);
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        List<JsonNode> array = node.asArray();
        List<Integer> indices = unmodifiableList(IntStream.range(0, array.size())
                .filter(i -> ctx.resolveInternalRefAndValidate(schemaRef, array.get(i)))
                .boxed()
                .collect(Collectors.toList()));
        return minContainsZero || !indices.isEmpty() ? Result.success(indices) : Result.failure("No items match contains");
    }
}

@SuppressWarnings("unchecked")
class AdditionalPropertiesEvaluator implements Evaluator {
    private final String schemaRef;

    AdditionalPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getAbsoluteUri(node);
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Set<String> props = new HashSet<>();
        props.addAll(ctx.getSiblingAnnotation(Keyword.PROPERTIES, Set.class).orElse(emptySet()));
        props.addAll(ctx.getSiblingAnnotation(Keyword.PATTERN_PROPERTIES, Set.class).orElse(emptySet()));
        Map<String, JsonNode> filtered = node.asObject().entrySet().stream()
                .filter(e -> !props.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        boolean valid = filtered.values().stream()
                .filter(prop -> ctx.resolveInternalRefAndValidate(schemaRef, prop))
                .count() == filtered.size();
        return valid ? Result.success(unmodifiableSet(filtered.keySet())) : Result.failure();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class PropertiesEvaluator implements Evaluator {
    private final Map<String, String> schemaRefs;

    PropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        Map<String, String> uris = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            uris.put(entry.getKey(), ctx.getAbsoluteUri(entry.getValue()));
        }
        this.schemaRefs = unmodifiableMap(uris);
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Map<String, JsonNode> filtered = node.asObject()
                .entrySet()
                .stream()
                .filter(e -> schemaRefs.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        boolean valid = filtered
                .entrySet()
                .stream()
                .map(e -> new AbstractMap.SimpleEntry<>(schemaRefs.get(e.getKey()), e.getValue()))
                .filter(e -> ctx.resolveInternalRefAndValidate(e.getKey(), e.getValue()))
                .count() == filtered.size();

        return valid ? Result.success(unmodifiableSet(filtered.keySet())) : Result.failure();
    }
}

class PatternPropertiesEvaluator implements Evaluator {
    private final Map<Pattern, String> schemasByPatterns;

    PatternPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.schemasByPatterns = node.asObject().entrySet().stream()
                .collect(Collectors.toMap(e -> Pattern.compile(e.getKey()), e -> ctx.getAbsoluteUri(e.getValue())));
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        boolean valid = true;
        Set<String> processed = new HashSet<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            List<String> schemaRefs = unmodifiableList(schemasByPatterns.entrySet().stream()
                    .filter(e -> e.getKey().matcher(entry.getKey()).find())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));
            if (!schemaRefs.isEmpty()) {
                processed.add(entry.getKey());
            }
            valid = schemaRefs.stream()
                    .filter(ref -> ctx.resolveInternalRefAndValidate(ref, entry.getValue()))
                    .count() == schemaRefs.size() && valid;
        }
        return valid ? Result.success(unmodifiableSet(processed)) : Result.failure();
    }
}

class DependentSchemasEvaluator implements Evaluator {
    private final Map<String, String> dependentSchemas;

    DependentSchemasEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.dependentSchemas = node.asObject().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ctx.getAbsoluteUri(e.getValue())));
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        List<String> fields = node.asObject().keySet()
                .stream()
                .filter(dependentSchemas::containsKey)
                .collect(Collectors.toList());
        List<String> failedFields = fields.stream()
                .filter(field -> !ctx.resolveInternalRefAndValidate(dependentSchemas.get(field), node))
                .collect(Collectors.toList());
        if (failedFields.isEmpty()) {
            return Result.success();
        } else {
            return Result.failure("Dependent schema validation failed for some properties " + failedFields);
        }
    }
}

class PropertyNamesEvaluator implements Evaluator {
    private final String schemaRef;

    PropertyNamesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getAbsoluteUri(node);
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Map<String, JsonNode> object = node.asObject();
        boolean valid = object.keySet().stream()
                .filter(propName -> ctx.resolveInternalRefAndValidate(schemaRef, new StringNode(propName, node.getJsonPointer())))
                .count() == object.size();

        return valid ? Result.success() : Result.failure();
    }
}

class IfThenElseEvaluator implements Evaluator {
    private final String ifRef;
    private final Optional<String> thenRef;
    private final Optional<String> elseRef;

    IfThenElseEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.ifRef = ctx.getAbsoluteUri(node);
        this.thenRef = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.THEN))
                .map(ctx::getAbsoluteUri);
        this.elseRef = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.ELSE))
                .map(ctx::getAbsoluteUri);
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (ctx.resolveInternalRefAndValidate(ifRef, node)) {
            boolean valid = thenRef
                    .map(ref -> ctx.resolveInternalRefAndValidate(ref, node))
                    .orElse(true);

            return valid ? Result.success() : Result.failure("Value matches against schema from 'if' but does not match against schema from 'then'");
        } else {
            boolean valid = elseRef
                    .map(ref -> ctx.resolveInternalRefAndValidate(ref, node))
                    .orElse(true);

            return valid ? Result.success() : Result.failure("Value does not match against schema from 'if' and 'else'");
        }
    }
}

class AllOfEvaluator implements Evaluator {
    private final List<String> refs;

    AllOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = unmodifiableList(node.asArray().stream().map(ctx::getAbsoluteUri).collect(Collectors.toList()));
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        List<Integer> unmatchedIndexes = IntStream.range(0, refs.size())
                .filter(i -> !ctx.resolveInternalRefAndValidate(refs.get(i), node))
                .boxed()
                .collect(Collectors.toList());

        if (unmatchedIndexes.isEmpty()) {
            return Result.success();
        }

        return Result.failure(String.format("Value does not match against the schemas at indexes %s", unmatchedIndexes));
    }
}

class AnyOfEvaluator implements Evaluator {
    private final List<String> refs;

    AnyOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = unmodifiableList(node.asArray().stream().map(ctx::getAbsoluteUri).collect(Collectors.toList()));
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        boolean valid = refs.stream()
                .filter(pointer -> ctx.resolveInternalRefAndValidate(pointer, node))
                .count() > 0;

        return valid ? Result.success() : Result.failure("Value does not match against any of the schemas");
    }
}

class OneOfEvaluator implements Evaluator {
    private final List<String> refs;

    OneOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = unmodifiableList(node.asArray().stream().map(ctx::getAbsoluteUri).collect(Collectors.toList()));
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        List<Integer> matchedIndexes = IntStream.range(0, refs.size())
                .filter(i -> ctx.resolveInternalRefAndValidate(refs.get(i), node))
                .boxed()
                .collect(Collectors.toList());

        if (matchedIndexes.size() == 1) {
            return Result.success();
        }

        if (matchedIndexes.isEmpty()) {
            return Result.failure("Value does not match against any of the schemas");
        }

        return Result.failure(String.format("Value matches against more than one schema. Matched schema indexes %s", matchedIndexes));
    }
}

class NotEvaluator implements Evaluator {
    private final String schemaUri;

    NotEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaUri = ctx.getAbsoluteUri(node);
    }

    @Override
    public Set<String> getVocabularies() {
        return APPLICATOR_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        boolean valid = !ctx.resolveInternalRefAndValidate(schemaUri, node);
        return valid ? Result.success() : Result.failure("Value matches against given schema but it must not");
    }
}

class UnevaluatedItemsEvaluator implements Evaluator {
    private final String schemaRef;
    private final String parentPath;

    UnevaluatedItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        String schemaPointer = node.getJsonPointer();
        this.schemaRef = ctx.getAbsoluteUri(schemaPointer);
        this.parentPath = UriUtil.getJsonPointerParent(schemaPointer);
    }

    @Override
    public Set<String> getVocabularies() {
        return UNEVALUATED_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        List<EvaluationItem> evaluationItems = unmodifiableList(ctx.getAnnotations().stream()
                .filter(a -> getSchemaPath(a).startsWith(parentPath))
                .collect(Collectors.toList()));
        List<JsonNode> array = node.asArray()
                .stream()
                .filter(arrayNode -> evaluationItems.stream().noneMatch(a -> a.getInstanceLocation().startsWith(arrayNode.getJsonPointer())))
                .collect(Collectors.toList());

        boolean valid = array.stream()
                .filter(arrayNode -> ctx.resolveInternalRefAndValidate(schemaRef, arrayNode))
                .count() == array.size();

        return valid ? Result.success() : Result.failure();
    }

    @Override
    public int getOrder() {
        return 30;
    }

    private String getSchemaPath(EvaluationItem item) {
        return UriUtil.getJsonPointer(item.getSchemaLocation());
    }
}

class UnevaluatedPropertiesEvaluator implements Evaluator {
    private final String schemaRef;
    private final String parentPath;

    UnevaluatedPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        String schemaPointer = node.getJsonPointer();
        this.schemaRef = ctx.getAbsoluteUri(schemaPointer);
        this.parentPath = UriUtil.getJsonPointerParent(schemaPointer);
    }

    @Override
    public Set<String> getVocabularies() {
        return UNEVALUATED_VOCABULARY;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        List<EvaluationItem> evaluationItems = unmodifiableList(ctx.getAnnotations().stream()
                .filter(a -> getSchemaPath(a).startsWith(parentPath))
                .collect(Collectors.toList()));

        List<JsonNode> array = node.asObject()
                .values()
                .stream()
                .filter(propertyNode -> evaluationItems.stream().noneMatch(a -> a.getInstanceLocation().startsWith(propertyNode.getJsonPointer())))
                .collect(Collectors.toList());

        boolean valid = array.stream()
                .filter(propertyNode -> ctx.resolveInternalRefAndValidate(schemaRef, propertyNode))
                .count() == array.size();

        return valid ? Result.success() : Result.failure();
    }

    @Override
    public int getOrder() {
        return 20;
    }

    private String getSchemaPath(EvaluationItem item) {
        return UriUtil.getJsonPointer(item.getSchemaLocation());
    }
}

class RefEvaluator implements Evaluator {
    private final String ref;

    RefEvaluator(JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.ref = node.asString();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        try {
            return ctx.resolveRefAndValidate(ref, node) ? Result.success() : Result.failure();
        } catch (SchemaNotFoundException e) {
            return Result.failure(String.format("Resolution of $ref [%s] failed", ref));
        }
    }

    @Override
    public Set<String> getVocabularies() {
        return Vocabulary.CORE_VOCABULARY;
    }
}

class DynamicRefEvaluator implements Evaluator {
    private final String ref;

    DynamicRefEvaluator(JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.ref = node.asString();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        try {
            return ctx.resolveDynamicRefAndValidate(ref, node) ? Result.success() : Result.failure();
        } catch (SchemaNotFoundException e) {
            return Result.failure(String.format("Resolution of $dynamicRef [%s] failed", ref));
        }
    }

    @Override
    public Set<String> getVocabularies() {
        return Vocabulary.CORE_VOCABULARY;
    }
}