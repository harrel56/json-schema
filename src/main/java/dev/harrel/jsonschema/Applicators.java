package dev.harrel.jsonschema;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class PrefixItemsEvaluator implements Applicator {
    private final List<String> prefixRefs;

    PrefixItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.prefixRefs = node.asArray().stream()
                .map(ctx::getAbsoluteUri)
                .toList();
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        List<JsonNode> elements = node.asArray();
        return IntStream.range(0, elements.size()).limit(prefixRefs.size()).boxed()
                .allMatch(idx -> ctx.resolveRequiredSchema(prefixRefs.get(idx)).validate(ctx, elements.get(idx)));
    }
}

class ItemsEvaluator implements Applicator {
    private final String schemaRef;
    private final int prefixItemsSize;

    ItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getAbsoluteUri(node);
        this.prefixItemsSize = Optional.ofNullable(ctx.getCurrentSchemaObject().get(Keyword.PREFIX_ITEMS))
                .map(JsonNode::asArray)
                .map(List::size)
                .orElse(0);
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }
        Schema schema = ctx.resolveRequiredSchema(schemaRef);
        return node.asArray().stream()
                .skip(prefixItemsSize)
                .allMatch(element -> schema.validate(ctx, element));
    }
}

class ContainsEvaluator implements Applicator {
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
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaRef);
        long count = node.asArray().stream()
                .filter(element -> schema.validate(ctx, element))
                .count();
        return count > 0 || minContainsZero;
    }
}

class AdditionalPropertiesEvaluator implements Applicator {
    private final String schemaRef;
    private final String parentPath;

    AdditionalPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        String schemaPointer = node.getJsonPointer();
        this.schemaRef = ctx.getAbsoluteUri(node);
        this.parentPath = schemaPointer.substring(0, schemaPointer.lastIndexOf('/'));
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        String instanceLocation = node.getJsonPointer();
        String propertiesPath = parentPath + "/" + Keyword.PROPERTIES;
        String patternPropertiesPath = parentPath + "/" + Keyword.PATTERN_PROPERTIES;
        Set<String> evaluatedInstances = ctx.getEvaluationItems().stream()
                .filter(a -> a.instanceLocation().startsWith(instanceLocation))
                .filter(a -> a.evaluationPath().startsWith(propertiesPath) || a.evaluationPath().startsWith(patternPropertiesPath))
                .map(EvaluationItem::instanceLocation)
                .collect(Collectors.toSet());
        Schema schema = ctx.resolveRequiredSchema(schemaRef);
        return node.asObject()
                .values()
                .stream()
                .filter(prop -> !evaluatedInstances.contains(prop.getJsonPointer()))
                .allMatch(prop -> schema.validate(ctx, prop));
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

class PropertiesEvaluator implements Applicator {
    private final Map<String, String> schemaRefs;

    PropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        Map<String, String> uris = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            uris.put(entry.getKey(), ctx.getAbsoluteUri(entry.getValue()));
        }
        this.schemaRefs = Collections.unmodifiableMap(uris);
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        return node.asObject()
                .entrySet()
                .stream()
                .filter(e -> schemaRefs.containsKey(e.getKey()))
                .map(e -> Map.entry(schemaRefs.get(e.getKey()), e.getValue()))
                .allMatch(e -> ctx.resolveRequiredSchema(e.getKey()).validate(ctx, e.getValue()));
    }
}

class PatternPropertiesEvaluator implements Applicator {
    private final Map<Pattern, String> schemasByPatterns;

    PatternPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalArgumentException();
        }
        this.schemasByPatterns = node.asObject().entrySet().stream()
                .collect(Collectors.toMap(e -> Pattern.compile(e.getKey()), e -> ctx.getAbsoluteUri(e.getValue())));
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            List<Schema> schemas = schemasByPatterns.entrySet().stream()
                    .filter(e -> e.getKey().matcher(entry.getKey()).find())
                    .map(Map.Entry::getValue)
                    .map(ctx::resolveRequiredSchema)
                    .toList();
            valid = schemas.stream().allMatch(schema -> schema.validate(ctx, entry.getValue())) && valid;
        }
        return valid;
    }
}

class DependentSchemasEvaluator implements Applicator {
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
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        return node.asObject().keySet()
                .stream()
                .filter(dependentSchemas::containsKey)
                .map(dependentSchemas::get)
                .map(ctx::resolveRequiredSchema)
                .allMatch(schema -> schema.validate(ctx, node));
    }
}

class PropertyNamesEvaluator implements Applicator {
    private final String schemaRef;

    PropertyNamesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaRef = ctx.getAbsoluteUri(node);
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaRef);
        return node.asObject().keySet().stream()
                .allMatch(propName -> schema.validate(ctx, new StringNode(propName, node.getJsonPointer())));
    }
}

class IfThenElseEvaluator implements Applicator {
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
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (ctx.resolveRequiredSchema(ifRef).validate(ctx, node)) {
            return thenRef
                    .map(ctx::resolveRequiredSchema)
                    .map(schema -> schema.validate(ctx, node)).orElse(true);
        } else {
            return elseRef
                    .map(ctx::resolveRequiredSchema)
                    .map(schema -> schema.validate(ctx, node)).orElse(true);
        }
    }
}

class AllOfEvaluator implements Applicator {
    private final List<String> refs;

    AllOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = node.asArray().stream().map(ctx::getAbsoluteUri).toList();
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        return refs.stream().allMatch(pointer -> ctx.resolveRequiredSchema(pointer).validate(ctx, node));
    }
}

class AnyOfEvaluator implements Applicator {
    private final List<String> refs;

    AnyOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = node.asArray().stream().map(ctx::getAbsoluteUri).toList();
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        return refs.stream()
                .filter(pointer -> ctx.resolveRequiredSchema(pointer).validate(ctx, node))
                .count() > 0;
    }
}

class OneOfEvaluator implements Applicator {
    private final List<String> refs;

    OneOfEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        this.refs = node.asArray().stream().map(ctx::getAbsoluteUri).toList();
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        return refs.stream()
                .filter(uri -> ctx.resolveRequiredSchema(uri).validate(ctx, node))
                .count() == 1;
    }
}

class NotEvaluator implements Applicator {
    private final String schemaUri;


    NotEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        this.schemaUri = ctx.getAbsoluteUri(node);
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        return !ctx.resolveRequiredSchema(schemaUri).validate(ctx, node);
    }
}

class UnevaluatedItemsEvaluator implements Applicator {
    private final String schemaRef;
    private final String parentPath;

    UnevaluatedItemsEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        String schemaPointer = node.getJsonPointer();
        this.schemaRef = ctx.getAbsoluteUri(schemaPointer);
        this.parentPath = schemaPointer.substring(0, schemaPointer.lastIndexOf('/'));
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaRef);
        List<EvaluationItem> evaluationItems = ctx.getEvaluationItems().stream()
                .filter(a -> a.evaluationPath().startsWith(parentPath))
                .toList();
        return node.asArray()
                .stream()
                .filter(arrayNode -> evaluationItems.stream().noneMatch(a -> a.instanceLocation().startsWith(arrayNode.getJsonPointer())))
                .allMatch(arrayNode -> schema.validate(ctx, arrayNode));
    }

    @Override
    public int getOrder() {
        return 20;
    }
}

class UnevaluatedPropertiesEvaluator implements Applicator {
    private final String schemaRef;
    private final String parentPath;

    UnevaluatedPropertiesEvaluator(SchemaParsingContext ctx, JsonNode node) {
        if (!node.isObject() && !node.isBoolean()) {
            throw new IllegalArgumentException();
        }
        String schemaPointer = node.getJsonPointer();
        this.schemaRef = ctx.getAbsoluteUri(schemaPointer);
        this.parentPath = schemaPointer.substring(0, schemaPointer.lastIndexOf('/'));
    }

    @Override
    public boolean apply(EvaluationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaRef);
        List<EvaluationItem> evaluationItems = ctx.getEvaluationItems().stream()
                .filter(a -> a.evaluationPath().startsWith(parentPath))
                .toList();
        return node.asObject()
                .values()
                .stream()
                .filter(propertyNode -> evaluationItems.stream().noneMatch(a -> a.instanceLocation().startsWith(propertyNode.getJsonPointer())))
                .allMatch(propertyNode -> schema.validate(ctx, propertyNode));
    }

    @Override
    public int getOrder() {
        return 20;
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
        Optional<Schema> schema = ctx.resolveSchema(ref);
        if (schema.isEmpty()) {
            return Result.failure("Resolution of $ref [%s] failed".formatted(ref));
        } else {
            return schema.get().validate(ctx, node) ? Result.success() : Result.failure();
        }
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
        Optional<Schema> schema = ctx.resolveDynamicSchema(ref);
        if (schema.isEmpty()) {
            return Result.failure("Resolution of $ref [%s] failed".formatted(ref));
        } else {
            return schema.get().validate(ctx, node) ? Result.success() : Result.failure();
        }
    }
}