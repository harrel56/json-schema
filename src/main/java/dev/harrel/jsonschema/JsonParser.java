package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

final class JsonParser {
    private final String defaultMetaSchemaUri;
    private final EvaluatorFactory evaluatorFactory;
    private final SchemaRegistry schemaRegistry;
    private final MetaSchemaValidator metaSchemaValidator;

    JsonParser(String defaultMetaSchemaUri,
               EvaluatorFactory evaluatorFactory,
               SchemaRegistry schemaRegistry,
               MetaSchemaValidator metaSchemaValidator) {
        this.defaultMetaSchemaUri = defaultMetaSchemaUri;
        this.evaluatorFactory = Objects.requireNonNull(evaluatorFactory);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.metaSchemaValidator = Objects.requireNonNull(metaSchemaValidator);
    }

    URI parseRootSchema(URI baseUri, JsonNode node) {
        if (node.isBoolean()) {
            metaSchemaValidator.validateMetaSchema(this, defaultMetaSchemaUri, baseUri.toString(), node);
            SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, baseUri.toString());
            boolean schemaValue = node.asBoolean();
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, List.of(new EvaluatorWrapper(null, node, Schema.getBooleanEvaluator(schemaValue))));
            return baseUri;
        } else if (node.isObject()) {
            Map<String, JsonNode> objectMap = node.asObject();
            String metaSchemaUri = Optional.ofNullable(objectMap.get(Keyword.SCHEMA))
                    .filter(JsonNode::isString)
                    .map(JsonNode::asString)
                    .orElse(defaultMetaSchemaUri);
            JsonNode idNode = objectMap.get(Keyword.ID);
            if (idNode != null && idNode.isString()) {
                String idString = idNode.asString();
                metaSchemaValidator.validateMetaSchema(this, metaSchemaUri, idString, node);
                if (!baseUri.toString().equals(idString)) {
                    SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, idString);
                    List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
                    schemaRegistry.registerIdentifiableSchema(ctx, URI.create(idString), node, evaluators);
                }
            } else {
                metaSchemaValidator.validateMetaSchema(this, metaSchemaUri, baseUri.toString(), node);
            }
            SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, baseUri.toString());
            List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, evaluators);

            return Optional.ofNullable(objectMap.get(Keyword.ID))
                    .filter(JsonNode::isString)
                    .map(JsonNode::asString)
                    .map(URI::create)
                    .orElse(baseUri);
        } else {
            throw new InvalidSchemaException("Schema [%s] was of invalid type [%s]".formatted(baseUri, node.getNodeType()), List.of());
        }
    }

    private void parseNode(SchemaParsingContext ctx, JsonNode node) {
        if (node.isBoolean()) {
            parseBoolean(ctx, node);
        } else if (node.isArray()) {
            parseArray(ctx, node);
        } else if (node.isObject()) {
            parseObject(ctx, node);
        }
    }

    private void parseBoolean(SchemaParsingContext ctx, JsonNode node) {
        boolean schemaValue = node.asBoolean();
        Evaluator booleanEvaluator = Schema.getBooleanEvaluator(schemaValue);
        schemaRegistry.registerSchema(ctx, node, List.of(new EvaluatorWrapper(null, node, booleanEvaluator)));
    }

    private void parseArray(SchemaParsingContext ctx, JsonNode node) {
        for (JsonNode element : node.asArray()) {
            parseNode(ctx, element);
        }
    }

    private void parseObject(SchemaParsingContext ctx, JsonNode node) {
        Map<String, JsonNode> objectMap = node.asObject();
        Optional<String> metaSchemaUri = Optional.ofNullable(objectMap.get(Keyword.SCHEMA))
                .filter(JsonNode::isString)
                .map(JsonNode::asString);
        JsonNode idNode = objectMap.get(Keyword.ID);
        if (idNode != null && idNode.isString()) {
            String idString = idNode.asString();
            metaSchemaUri.ifPresent(uri -> metaSchemaValidator.validateMetaSchema(this, uri, idString, node));
            URI uri = ctx.getParentUri().resolve(idString);
            SchemaParsingContext newCtx = ctx.withParentUri(uri);
            List<EvaluatorWrapper> evaluators = parseEvaluators(newCtx, objectMap, node.getJsonPointer());
            schemaRegistry.registerIdentifiableSchema(newCtx, uri, node, evaluators);
        } else {
            metaSchemaUri.ifPresent(uri -> metaSchemaValidator.validateMetaSchema(this, uri, ctx.getAbsoluteUri(node), node));
            schemaRegistry.registerSchema(ctx, node, parseEvaluators(ctx, objectMap, node.getJsonPointer()));
        }
    }

    private List<EvaluatorWrapper> parseEvaluators(SchemaParsingContext ctx, Map<String, JsonNode> object, String objectPath) {
        SchemaParsingContext newCtx = ctx.withCurrentSchemaContext(object);
        List<EvaluatorWrapper> evaluators = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : object.entrySet()) {
            evaluatorFactory.create(newCtx, entry.getKey(), entry.getValue())
                    .map(evaluator -> new EvaluatorWrapper(entry.getKey(), entry.getValue(), evaluator))
                    .ifPresent(evaluators::add);
            parseNode(newCtx, entry.getValue());
        }
        if (evaluators.isEmpty()) {
            evaluators.add(new EvaluatorWrapper(null, objectPath, Schema.getBooleanEvaluator(true)));
        }
        return evaluators;
    }
}

