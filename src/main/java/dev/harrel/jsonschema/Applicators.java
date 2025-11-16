package dev.harrel.jsonschema;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        Object annotation = size == elements.size() ? Boolean.TRUE : prefixRefs.size();
        return valid ? Result.success(annotation) : Result.annotatedFailure(annotation);
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }
        List<JsonNode> array = node.asArray();
        Object prefixItemsAnnotation = ctx.getSiblingAnnotation(Keyword.PREFIX_ITEMS);
        if (prefixItemsAnnotation instanceof Boolean) {
            return Result.success();
        }

        int prefixItemsSize = prefixItemsAnnotation instanceof Integer ? (Integer) prefixItemsAnnotation : 0;
        boolean valid = true;
        for (int i = prefixItemsSize; i < array.size(); i++) {
            valid = ctx.resolveInternalRefAndValidate(schemaRef, array.get(i)) && valid;
        }
        return valid ? Result.success(true) : Result.annotatedFailure(true);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class ItemsLegacyEvaluator implements Evaluator {
    private final CompoundUri schemaRef;
    private final List<CompoundUri> schemaRefs;

    ItemsLegacyEvaluator(SchemaParsingContext ctx, JsonNode node) {
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }
        List<JsonNode> array = node.asArray();
        if (schemaRef != null) {
            boolean valid = true;
            for (JsonNode element : array) {
                valid = ctx.resolveInternalRefAndValidate(schemaRef, element) && valid;
            }
            return valid ? Result.success(true) : Result.annotatedFailure(true);
        } else {
            int size = Math.min(schemaRefs.size(), array.size());
            boolean valid = true;
            for (int i = 0; i < size; i++) {
                valid = ctx.resolveInternalRefAndValidate(schemaRefs.get(i), array.get(i)) && valid;
            }
            Object annotation = size == array.size() ? Boolean.TRUE : schemaRefs.size();
            return valid ? Result.success(annotation) : Result.annotatedFailure(annotation);
        }
    }
}

class AdditionalItemsEvaluator implements Evaluator {
    private final CompoundUri schemaRef;

    AdditionalItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }
        List<JsonNode> array = node.asArray();
        Object itemsAnnotation = ctx.getSiblingAnnotation(Keyword.ITEMS);
        if (itemsAnnotation instanceof Boolean || itemsAnnotation == null) {
            return Result.success();
        }

        boolean valid = true;
        for (int i = (Integer) itemsAnnotation; i < array.size(); i++) {
            valid = ctx.resolveInternalRefAndValidate(schemaRef, array.get(i)) && valid;
        }
        return valid ? Result.success(true) : Result.annotatedFailure(true);
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        List<JsonNode> array = node.asArray();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            if (ctx.resolveInternalRefAndValidate(schemaRef, array.get(i))) {
                indices.add(i);
            }
        }
        return minContainsZero || !indices.isEmpty() ? Result.success(indices) : Result.formattedFailure("contains");
    }
}

class AdditionalPropertiesEvaluator implements Evaluator {
    private final CompoundUri schemaRef;
    /* To reduce annotation usage when not needed */
    private final Set<String> propertyNames;
    private final boolean hasPatternProperties;

    AdditionalPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);

        Set<String> tmpProps = emptySet();
        JsonNode propertiesNode = ctx.getCurrentSchemaObject().get(Keyword.PROPERTIES);
        if (propertiesNode != null && propertiesNode.isObject()) {
            tmpProps = propertiesNode.asObject().keySet();
        }
        this.propertyNames = unmodifiableSet(tmpProps);
        this.hasPatternProperties = ctx.getCurrentSchemaObject().containsKey(Keyword.PATTERN_PROPERTIES);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Set<String> patternNames = emptySet();
        if (hasPatternProperties) {
            Object patternAnnotation = ctx.getSiblingAnnotation(Keyword.PATTERN_PROPERTIES);
            if (patternAnnotation instanceof Set) {
                patternNames = (Set<String>) patternAnnotation;
            }
        }

        Map<String, JsonNode> objectMap = node.asObject();
        List<String> processed = new ArrayList<>(objectMap.size());
        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            if (!propertyNames.contains(key) && !patternNames.contains(key)) {
                processed.add(key);
                valid = ctx.resolveInternalRefAndValidate(schemaRef, entry.getValue()) && valid;
            }
        }
        return valid ? Result.success(unmodifiableList(processed)) : Result.annotatedFailure(unmodifiableList(processed));
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Set<String> processed = new HashSet<>();
        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            CompoundUri ref = schemaRefs.get(entry.getKey());
            if (ref != null) {
                processed.add(entry.getKey());
                valid = ctx.resolveInternalRefAndValidate(ref, entry.getValue()) && valid;
            }
        }
        return valid ? Result.success(unmodifiableSet(processed)) : Result.annotatedFailure(unmodifiableSet(processed));
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        boolean valid = true;
        Set<String> processed = new HashSet<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            for (Map.Entry<Pattern, CompoundUri> patternEntry : schemasByPatterns.entrySet()) {
                if (patternEntry.getKey().matcher(entry.getKey()).find()) {
                    processed.add(entry.getKey());
                    valid = ctx.resolveInternalRefAndValidate(patternEntry.getValue(), entry.getValue()) && valid;
                }
            }
        }
        return valid ? Result.success(unmodifiableSet(processed)) : Result.annotatedFailure(unmodifiableSet(processed));
    }
}

class DependentSchemasEvaluator implements Evaluator {
    private final Map<String, CompoundUri> dependentSchemas;

    DependentSchemasEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.dependentSchemas = toMap(ctx, node.asObject());
    }

    DependentSchemasEvaluator(SchemaParsingContext ctx, Map<String, JsonNode> objectNode) {
        this.dependentSchemas = toMap(ctx, objectNode);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        List<String> failedFields = new ArrayList<>();
        for (Map.Entry<String, JsonNode> e : node.asObject().entrySet()) {
            CompoundUri ref = dependentSchemas.get(e.getKey());
            if (ref != null && !ctx.resolveInternalRefAndValidate(ref, node)) {
                failedFields.add(e.getKey());
            }
        }
        if (failedFields.isEmpty()) {
            return Result.success();
        } else {
            return Result.formattedFailure("dependentSchemas", failedFields);
        }
    }

    private static Map<String, CompoundUri> toMap(SchemaParsingContext ctx, Map<String, JsonNode> objectNode) {
        return objectNode.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ctx.getCompoundUri(e.getValue())));
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        boolean valid = true;
        for (String propName : node.asObject().keySet()) {
            valid = ctx.resolveInternalRefAndValidate(schemaRef, new StringNode(propName, node.getJsonPointer())) && valid;
        }
        return valid ? Result.success() : Result.failure();
    }
}

class IfThenElseEvaluator implements Evaluator {
    private final CompoundUri ifRef;
    private final CompoundUri thenRef;
    private final CompoundUri elseRef;

    IfThenElseEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.ifRef = ctx.getCompoundUri(node);
        JsonNode thenNode = ctx.getCurrentSchemaObject().get(Keyword.THEN);
        this.thenRef = thenNode == null ? null : ctx.getCompoundUri(thenNode);
        JsonNode elseNode = ctx.getCurrentSchemaObject().get(Keyword.ELSE);
        this.elseRef = elseNode == null ? null : ctx.getCompoundUri(elseNode);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (ctx.resolveInternalRefAndValidate(ifRef, node)) {
            boolean valid = thenRef == null || ctx.resolveInternalRefAndValidate(thenRef, node);
            return valid ? Result.success() : Result.formattedFailure("ifThen");
        } else {
            boolean valid = elseRef == null || ctx.resolveInternalRefAndValidate(elseRef, node);
            return valid ? Result.success() : Result.formattedFailure("ifElse");
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        List<Integer> unmatchedIndexes = new ArrayList<>();
        for (int i = 0; i < refs.size(); i++) {
            if (!ctx.resolveInternalRefAndValidate(refs.get(i), node)) {
                unmatchedIndexes.add(i);
            }
        }

        if (unmatchedIndexes.isEmpty()) {
            return Result.success();
        }
        return Result.formattedFailure("allOf", unmatchedIndexes);
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        boolean valid = false;
        for (CompoundUri ref : refs) {
            valid = ctx.resolveInternalRefAndValidate(ref, node) || valid;
        }
        return valid ? Result.success() : Result.formattedFailure("anyOf");
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        List<Integer> matchedIndexes = new ArrayList<>();
        for (int i = 0; i < refs.size(); i++) {
            if (ctx.resolveInternalRefAndValidate(refs.get(i), node)) {
                matchedIndexes.add(i);
            }
        }

        if (matchedIndexes.size() == 1) {
            return Result.success();
        }
        if (matchedIndexes.isEmpty()) {
            return Result.failure("Value does not match against any of the schemas");
        }
        return Result.formattedFailure("oneOf", matchedIndexes);
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
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        boolean valid = !ctx.resolveInternalRefAndValidate(schemaUri, node);
        return valid ? Result.success() : Result.formattedFailure("not");
    }
}

class UnevaluatedItemsEvaluator implements Evaluator {
    private final CompoundUri schemaRef;

    UnevaluatedItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return Result.success();
        }

        Map.Entry<Integer, Set<Integer>> evaluated = ctx.calculateEvaluatedItems(node.getJsonPointer());
        List<JsonNode> array = node.asArray();
        if (evaluated.getKey() >= array.size()) {
            return Result.success();
        }
        Set<Integer> evaluatedIndices = evaluated.getValue();
        boolean valid = true;
        for (int i = evaluated.getKey(); i < array.size(); i++) {
            if (!evaluatedIndices.contains(i)) {
                valid = ctx.resolveInternalRefAndValidate(schemaRef, array.get(i)) && valid;
            }
        }
        return valid ? Result.success(true) : Result.failure();
    }

    @Override
    public int getOrder() {
        return 30;
    }
}

class UnevaluatedPropertiesEvaluator implements Evaluator {
    private final CompoundUri schemaRef;

    UnevaluatedPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getCompoundUri(node);
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return Result.success();
        }

        Set<String> evaluatedInstances = ctx.calculateEvaluatedProperties(node.getJsonPointer());
        Set<String> processed = new HashSet<>();
        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            if (!evaluatedInstances.contains(entry.getKey())) {
                processed.add(entry.getKey());
                valid = ctx.resolveInternalRefAndValidate(schemaRef, entry.getValue()) && valid;
            }
        }
        return valid ? Result.success(processed) : Result.failure();
    }

    @Override
    public int getOrder() {
        return 20;
    }
}

class RefEvaluator implements Evaluator {
    private final CompoundUri ref;

    RefEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.ref = UriUtil.resolveUri(ctx.getParentUri(), CompoundUri.fromString(node.asString()));
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        try {
            return ctx.resolveRefAndValidate(ref, node) ? Result.success() : Result.failure();
        } catch (SchemaNotFoundException e) {
            return Result.formattedFailure("$ref", ref);
        }
    }
}

class DynamicRefEvaluator implements Evaluator {
    private final CompoundUri ref;

    DynamicRefEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        this.ref = UriUtil.resolveUri(ctx.getParentUri(), CompoundUri.fromString(node.asString()));
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        try {
            return ctx.resolveDynamicRefAndValidate(ref, node) ? Result.success() : Result.failure();
        } catch (SchemaNotFoundException e) {
            return Result.formattedFailure("$dynamicRef", ref);
        }
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
            return Result.formattedFailure("$recursiveRef", ref);
        }
    }
}

class LegacyRefEvaluator implements Evaluator {
    private final CompoundUri ref;

    LegacyRefEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isString()) {
            throw new IllegalArgumentException();
        }
        if (ctx.getCurrentSchemaObject().containsKey(Keyword.getIdKeyword(ctx.getDialect().getSpecificationVersion()))) {
            this.ref = UriUtil.resolveUri(ctx.getGrandparentUri(), CompoundUri.fromString(node.asString()));
        } else {
            this.ref = UriUtil.resolveUri(ctx.getParentUri(), CompoundUri.fromString(node.asString()));
        }
    }

    @Override
    public Result evaluate(EvaluationContext ctx, JsonNode node) {
        try {
            return ctx.resolveRefAndValidate(ref, node) ? Result.success() : Result.failure();
        } catch (SchemaNotFoundException e) {
            return Result.formattedFailure("$ref", ref);
        }
    }
}