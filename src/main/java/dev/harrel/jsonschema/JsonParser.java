package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

final class JsonParser {
    private final Dialect dialect;
    private final EvaluatorFactory evaluatorFactory;
    private final SchemaRegistry schemaRegistry;
    private final MetaSchemaValidator metaSchemaValidator;

    JsonParser(Dialect dialect,
               EvaluatorFactory evaluatorFactory,
               SchemaRegistry schemaRegistry,
               MetaSchemaValidator metaSchemaValidator) {
        this.dialect = Objects.requireNonNull(dialect);
        this.evaluatorFactory = Objects.requireNonNull(evaluatorFactory);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.metaSchemaValidator = Objects.requireNonNull(metaSchemaValidator);
    }

    URI parseRootSchema(URI baseUri, JsonNode node) {
        Optional<Map<String, JsonNode>> objectMapOptional = JsonNodeUtil.getAsObject(node);
        String metaSchemaUri = objectMapOptional
                .flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.SCHEMA))
                .orElse(dialect.getMetaSchema());
        Optional<String> providedSchemaId = objectMapOptional
                .flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.ID))
                .filter(id -> !baseUri.toString().equals(id));

        MetaValidationResult metaValidationResult = validateSchemaOrPostpone(node, metaSchemaUri, baseUri.toString(), providedSchemaId);
        providedSchemaId.ifPresent(JsonNodeUtil::validateIdField);

        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, baseUri.toString(), emptyMap());
            List<EvaluatorWrapper> evaluators = singletonList(new EvaluatorWrapper(null, node, Schema.getBooleanEvaluator(node.asBoolean())));
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, evaluators, metaValidationResult.activeVocabularies);
        } else if (objectMapOptional.isPresent()) {
            Map<String, JsonNode> objectMap = objectMapOptional.get();
            if (providedSchemaId.isPresent()) {
                String idString = providedSchemaId.get();
                SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, idString, objectMap);
                List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
                schemaRegistry.registerIdentifiableSchema(ctx, URI.create(idString), node, evaluators, metaValidationResult.activeVocabularies);
            }
            SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, baseUri.toString(), objectMap);
            List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, evaluators, metaValidationResult.activeVocabularies);
        }

        metaValidationResult.performPostponedSchemaValidation(node, metaSchemaUri, baseUri.toString(), providedSchemaId);

        return providedSchemaId.map(URI::create).orElse(baseUri);
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
        List<EvaluatorWrapper> evaluators = singletonList(new EvaluatorWrapper(null, node, booleanEvaluator));
        schemaRegistry.registerSchema(ctx, node, evaluators, dialect.getSupportedVocabularies());
    }

    private void parseArray(SchemaParsingContext ctx, JsonNode node) {
        for (JsonNode element : node.asArray()) {
            parseNode(ctx, element);
        }
    }

    private void parseObject(SchemaParsingContext ctx, JsonNode node) {
        Map<String, JsonNode> objectMap = node.asObject();
        String metaSchemaUri = JsonNodeUtil.getStringField(objectMap, Keyword.SCHEMA).orElse(null);
        Optional<String> providedSchemaId = JsonNodeUtil.getStringField(objectMap, Keyword.ID);
        providedSchemaId.ifPresent(JsonNodeUtil::validateIdField);
        String absoluteUri = ctx.getAbsoluteUri(node);
        MetaValidationResult metaValidationResult = validateSchemaOrPostpone(node, metaSchemaUri, absoluteUri, providedSchemaId);

        if (providedSchemaId.isPresent()) {
            String idString = providedSchemaId.get();
            URI uri = ctx.getParentUri().resolve(idString);
            SchemaParsingContext newCtx = ctx.withParentUri(uri);
            List<EvaluatorWrapper> evaluators = parseEvaluators(newCtx, objectMap, node.getJsonPointer());
            schemaRegistry.registerIdentifiableSchema(newCtx, uri, node, evaluators, metaValidationResult.activeVocabularies);
        } else {
            schemaRegistry.registerSchema(ctx, node, parseEvaluators(ctx, objectMap, node.getJsonPointer()), metaValidationResult.activeVocabularies);
        }

        metaValidationResult.performPostponedSchemaValidation(node, metaSchemaUri, absoluteUri, providedSchemaId);
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
    private MetaValidationResult validateSchemaOrPostpone(JsonNode node, String metaSchemaUri, String baseUri, Optional<String> providedSchemaId) {
        if (metaSchemaUri == null) {
            return new MetaValidationResult(dialect.getSupportedVocabularies(), null);
        } else if (!baseUri.equals(metaSchemaUri) && providedSchemaId.map(id -> !id.equals(metaSchemaUri)).orElse(true)) {
            Set<String> activeVocabularies = metaSchemaValidator.validateSchema(this, metaSchemaUri, providedSchemaId.orElse(baseUri), node);
            return new MetaValidationResult(activeVocabularies, null);
        } else {
            Map<String, Boolean> vocabularyObject = JsonNodeUtil.getAsObject(node)
                    .flatMap(JsonNodeUtil::getVocabulariesObject)
                    .orElse(dialect.getDefaultVocabularyObject());
            Set<String> activeVocabularies = metaSchemaValidator.determineActiveVocabularies(vocabularyObject);
            return new MetaValidationResult(activeVocabularies, schemaRegistry.createSnapshot());
        }
    }

    private final class MetaValidationResult {
        private final Set<String> activeVocabularies;
        private final SchemaRegistry.State recoveryState;

        private MetaValidationResult(Set<String> activeVocabularies, SchemaRegistry.State recoveryState) {
            this.activeVocabularies = Objects.requireNonNull(activeVocabularies);
            this.recoveryState = recoveryState;
        }

        private void performPostponedSchemaValidation(JsonNode node,
                                                      String metaSchemaUri,
                                                      String baseUri,
                                                      Optional<String> providedSchemaId) {
            if (recoveryState == null) {
                return;
            }

            try {
                metaSchemaValidator.validateSchema(JsonParser.this, metaSchemaUri, providedSchemaId.orElse(baseUri), node);
            } catch (JsonSchemaException e) {
                schemaRegistry.restoreSnapshot(recoveryState);
                throw e;
            }
        }
    }
}

