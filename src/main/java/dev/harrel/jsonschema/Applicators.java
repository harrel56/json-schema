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
    private final List<CompoundUri> prefixRefs;

    PrefixItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.prefixRefs = unmodifiableList(node.asArray().stream()
                .map(ctx::getCompoundUri)
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
        boolean valid = true;
        for (int i = 0; i < size; i++) {
            valid = ctx.resolveInternalRefAndValidate(prefixRefs.get(i), elements.get(i)) && valid;
        }
        return valid ? Result.success(prefixRefs.size()) : Result.failure();
    }
}

class ItemsEvaluator implements Evaluator {
    private final CompoundUri schemaRef;

    ItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
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

class Items2019Evaluator implements Evaluator {
    private final CompoundUri schemaRef;
    private final List<CompoundUri> schemaRefs;

    Items2019Evaluator(SchemaParsingContext ctx, JsonNode node) {
        if (node.isObject() || node.isBoolean()) {
            this.schemaRef = ctx.getCompoundUri(node);
            this.schemaRefs = null;
        } else if (node.isArray()) {
            this.schemaRef = null;
            this.schemaRefs = unmodifiableList(node.asArray().stream()
                    .map(ctx::getCompoundUri)
                    .collect(Collectors.toList()));
        } else {
            throw new IllegalArgumentException();
        }
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
        if (schemaRef != null) {
            boolean valid = array.stream()
                    .filter(element -> ctx.resolveInternalRefAndValidate(schemaRef, element))
                    .count() == array.size();
            return valid ? Result.success(true) : Result.failure();
        } else {
            int size = Math.min(schemaRefs.size(), array.size());
            boolean valid = IntStream.range(0, size)
                    .boxed()
                    .filter(idx -> ctx.resolveInternalRefAndValidate(schemaRefs.get(idx), array.get(idx)))
                    .count() == size;
            return valid ? Result.success(schemaRefs.size()) : Result.failure();
        }
    }
}

class AdditionalItemsEvaluator implements Evaluator {
    private static final Set<String> vocabularies = singleton(Vocabulary.Draft2019.APPLICATOR);
    private final CompoundUri schemaRef;

    AdditionalItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
    }

    @Override
    public Set<String> getVocabularies() {
        return vocabularies;
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }
        List<JsonNode> array = node.asArray();
        Optional<Object> itemsAnnotation = ctx.getSiblingAnnotation(Keyword.ITEMS);
        boolean shouldSkip = itemsAnnotation.map(Boolean.class::isInstance).orElse(false);
        if (shouldSkip) {
            return Result.success(true);
        }
        int itemsSize = itemsAnnotation.filter(Integer.class::isInstance).map(Integer.class::cast).orElse(array.size());
        int size = Math.max(array.size() - itemsSize, 0);
        boolean valid = array.stream()
                .skip(itemsSize)
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
    private final CompoundUri schemaRef;
    private final boolean minContainsZero;

    ContainsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
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
        return minContainsZero || !indices.isEmpty() ? Result.success(indices) : Result.failure("Array contains no matching items");
    }
}

@SuppressWarnings("unchecked")
class AdditionalPropertiesEvaluator implements Evaluator {
    private final CompoundUri schemaRef;

    AdditionalPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
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
    private final Map<String, CompoundUri> schemaRefs;

    PropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        Map<String, CompoundUri> uris = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            uris.put(entry.getKey(), ctx.getCompoundUri(entry.getValue()));
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

        Set<String> fields = new HashSet<>();
        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            CompoundUri ref = schemaRefs.get(entry.getKey());
            if (ref != null) {
                fields.add(entry.getKey());
                valid = ctx.resolveInternalRefAndValidate(ref, entry.getValue()) && valid;
            }
        }
        return valid ? Result.success(unmodifiableSet(fields)) : Result.failure();
    }
}

class PatternPropertiesEvaluator implements Evaluator {
    private final Map<Pattern, CompoundUri> schemasByPatterns;

    PatternPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.schemasByPatterns = node.asObject().entrySet().stream()
                .collect(Collectors.toMap(e -> Pattern.compile(e.getKey()), e -> ctx.getCompoundUri(e.getValue())));
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
            List<CompoundUri> schemaRefs = unmodifiableList(schemasByPatterns.entrySet().stream()
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
    private final Map<String, CompoundUri> dependentSchemas;

    DependentSchemasEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.dependentSchemas = node.asObject().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ctx.getCompoundUri(e.getValue())));
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
            return Result.failure(String.format("Object does not match dependent schemas for some properties %s", failedFields));
        }
    }
}

class PropertyNamesEvaluator implements Evaluator {
    private final CompoundUri schemaRef;

    PropertyNamesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
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
    private final CompoundUri ifRef;
    private final Optional<CompoundUri> thenRef;
    private final Optional<CompoundUri> elseRef;

    IfThenElseEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.ifRef = ctx.getCompoundUri(node);
        this.thenRef = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.THEN))
                .map(ctx::getCompoundUri);
        this.elseRef = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.ELSE))
                .map(ctx::getCompoundUri);
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
    private final List<CompoundUri> refs;

    AllOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = unmodifiableList(node.asArray().stream().map(ctx::getCompoundUri).collect(Collectors.toList()));
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
    private final List<CompoundUri> refs;

    AnyOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = unmodifiableList(node.asArray().stream().map(ctx::getCompoundUri).collect(Collectors.toList()));
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
    private final List<CompoundUri> refs;

    OneOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = unmodifiableList(node.asArray().stream().map(ctx::getCompoundUri).collect(Collectors.toList()));
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
    private final CompoundUri schemaUri;

    NotEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaUri = ctx.getCompoundUri(node);
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
    private final CompoundUri schemaRef;
    private final String parentPath;

    UnevaluatedItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
        this.parentPath = UriUtil.getJsonPointerParent(schemaRef.fragment);
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

        Set<String> evaluatedInstances = ctx.getAnnotationsFromParent(parentPath)
                .map(Annotation::getInstanceLocation)
                .collect(Collectors.toSet());
        List<JsonNode> array = node.asArray()
                .stream()
                .filter(arrayNode -> !evaluatedInstances.contains(arrayNode.getJsonPointer()))
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
}

class UnevaluatedPropertiesEvaluator implements Evaluator {
    private final CompoundUri schemaRef;
    private final String parentPath;

    UnevaluatedPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
        this.parentPath = UriUtil.getJsonPointerParent(schemaRef.fragment);
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

        Set<String> evaluatedInstances = ctx.getAnnotationsFromParent(parentPath)
                .map(Annotation::getInstanceLocation)
                .collect(Collectors.toSet());

        List<JsonNode> array = node.asObject()
                .values()
                .stream()
                .filter(propertyNode -> !evaluatedInstances.contains(propertyNode.getJsonPointer()))
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
}

class RefEvaluator implements Evaluator {
    private final CompoundUri ref;

    RefEvaluator(JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.ref = CompoundUri.fromString(node.asString());
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
    private final CompoundUri ref;

    DynamicRefEvaluator(JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.ref = CompoundUri.fromString(node.asString());
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

class RecursiveRefEvaluator implements Evaluator {
    private final String ref;

    RecursiveRefEvaluator(JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.ref = node.asString();
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        try {
            return ctx.resolveRecursiveRefAndValidate(ref, node) ? Result.success() : Result.failure();
        } catch (SchemaNotFoundException e) {
            return Result.failure(String.format("Resolution of $recursiveRef [%s] failed", ref));
        }
    }

    @Override
    public Set<String> getVocabularies() {
        return Vocabulary.CORE_VOCABULARY;
    }
}