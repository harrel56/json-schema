package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static java.util.Collections.singletonList;

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
        Optional<Map<String, JsonNode>> objectMapOptional = getAsObject(node);
        String metaSchemaUri = objectMapOptional
                .map(obj -> obj.get(Keyword.SCHEMA))
                .filter(JsonNode::isString)
                .map(JsonNode::asString)
                .orElse(defaultMetaSchemaUri);
        Optional<String> providedSchemaId = objectMapOptional
                .map(obj -> obj.get(Keyword.ID))
                .filter(JsonNode::isString)
                .map(JsonNode::asString)
                .filter(id -> !baseUri.toString().equals(id));

        Optional<SchemaRegistry.State> registryPreviousState = validateSchemaOrPostpone(node, metaSchemaUri, baseUri.toString(), providedSchemaId);

        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, baseUri.toString());
            boolean schemaValue = node.asBoolean();
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, singletonList(new EvaluatorWrapper(null, node, Schema.getBooleanEvaluator(schemaValue))));
        } else if (objectMapOptional.isPresent()) {
            Map<String, JsonNode> objectMap = objectMapOptional.get();
            if (providedSchemaId.isPresent()) {
                String idString = providedSchemaId.get();
                SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, idString);
                List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
                schemaRegistry.registerIdentifiableSchema(ctx, URI.create(idString), node, evaluators);
            }
            SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, baseUri.toString());
            List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, evaluators);
        }

        registryPreviousState.ifPresent(state -> performPostponedSchemaValidation(state, node, metaSchemaUri, baseUri.toString(), providedSchemaId));

        return providedSchemaId.map(URI::create).orElse(baseUri);
    }

    private Optional<Map<String, JsonNode>> getAsObject(JsonNode node) {
        return node.isObject() ? Optional.of(node.asObject()) : Optional.empty();
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
        schemaRegistry.registerSchema(ctx, node, singletonList(new EvaluatorWrapper(null, node, booleanEvaluator)));
    }

    private void parseArray(SchemaParsingContext ctx, JsonNode node) {
        for (JsonNode element : node.asArray()) {
            parseNode(ctx, element);
        }
    }

    private void parseObject(SchemaParsingContext ctx, JsonNode node) {
        Map<String, JsonNode> objectMap = node.asObject();
        String metaSchemaUri = Optional.ofNullable(objectMap.get(Keyword.SCHEMA))
                .filter(JsonNode::isString)
                .map(JsonNode::asString)
                .orElse(null);
        Optional<String> providedSchemaId = Optional.ofNullable(objectMap.get(Keyword.ID))
                .filter(JsonNode::isString)
                .map(JsonNode::asString);
        String absoluteUri = ctx.getAbsoluteUri(node);
        Optional<SchemaRegistry.State> registryPreviousState = validateSchemaOrPostpone(node, metaSchemaUri, absoluteUri, providedSchemaId);

        if (providedSchemaId.isPresent()) {
            String idString = providedSchemaId.get();
            URI uri = ctx.getParentUri().resolve(idString);
            SchemaParsingContext newCtx = ctx.withParentUri(uri);
            List<EvaluatorWrapper> evaluators = parseEvaluators(newCtx, objectMap, node.getJsonPointer());
            schemaRegistry.registerIdentifiableSchema(newCtx, uri, node, evaluators);
        } else {
            schemaRegistry.registerSchema(ctx, node, parseEvaluators(ctx, objectMap, node.getJsonPointer()));
        }

        registryPreviousState.ifPresent(state -> performPostponedSchemaValidation(state, node, metaSchemaUri, absoluteUri, providedSchemaId));
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

    /* If meta-schema is the same as schema, its validation needs to be postponed */
    private Optional<SchemaRegistry.State> validateSchemaOrPostpone(JsonNode node, String metaSchemaUri, String baseUri, Optional<String> providedSchemaId) {
        if (metaSchemaUri == null) {
            return Optional.empty();
        } else if (!baseUri.equals(metaSchemaUri) && providedSchemaId.map(id -> !id.equals(metaSchemaUri)).orElse(true)) {
            metaSchemaValidator.validateMetaSchema(this, metaSchemaUri, providedSchemaId.orElse(baseUri), node);
            return Optional.empty();
        } else {
            return Optional.of(schemaRegistry.createSnapshot());
        }
    }

    private void performPostponedSchemaValidation(SchemaRegistry.State previousState,
                                                  JsonNode node,
                                                  String metaSchemaUri,
                                                  String baseUri,
                                                  Optional<String> providedSchemaId) {
        try {
            metaSchemaValidator.validateMetaSchema(this, metaSchemaUri, providedSchemaId.orElse(baseUri), node);
        } catch (JsonSchemaException e) {
            schemaRegistry.restoreSnapshot(previousState);
            throw e;
        }
    }
}

