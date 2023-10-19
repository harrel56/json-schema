package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

final class JsonParser {
    private final Dialect dialect;
    private final EvaluatorFactory evaluatorFactory;
    private final SchemaRegistry schemaRegistry;
    private final MetaSchemaValidator metaSchemaValidator;
    private final Map<String, MetaSchemaData> unfinishedSchemas = new HashMap<>();

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
        Map<String, Boolean> vocabulariesObject = objectMapOptional
                .flatMap(JsonNodeUtil::getVocabulariesObject)
                .orElse(dialect.getDefaultVocabularyObject());

        MetaSchemaData metaSchemaData = new MetaSchemaData(vocabulariesObject);
        unfinishedSchemas.put(baseUri.toString(), metaSchemaData);
        providedSchemaId.ifPresent(id -> unfinishedSchemas.put(id, metaSchemaData));

        String finalUri = providedSchemaId.orElse(baseUri.toString());
        Set<String> activeVocabularies = validateSchemaOrPostpone(node, metaSchemaUri, finalUri);
        providedSchemaId.ifPresent(JsonNodeUtil::validateIdField);

        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, baseUri.toString(), emptyMap());
            List<EvaluatorWrapper> evaluators = singletonList(new EvaluatorWrapper(null, node, Schema.getBooleanEvaluator(node.asBoolean())));
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, evaluators, activeVocabularies);
        } else if (objectMapOptional.isPresent()) {
            Map<String, JsonNode> objectMap = objectMapOptional.get();
            if (providedSchemaId.isPresent()) {
                String idString = providedSchemaId.get();
                SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, idString, objectMap);
                List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
                schemaRegistry.registerIdentifiableSchema(ctx, URI.create(idString), node, evaluators, activeVocabularies);
            }
            SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, baseUri.toString(), objectMap);
            List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, evaluators, activeVocabularies);
        }

        metaSchemaData.parsed();
        unfinishedSchemas.remove(baseUri.toString());
        providedSchemaId.ifPresent(unfinishedSchemas::remove);

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
        Map<String, Boolean> vocabularyObject = JsonNodeUtil.getVocabulariesObject(objectMap)
                .orElse(dialect.getDefaultVocabularyObject());
        MetaSchemaData metaSchemaData = new MetaSchemaData(vocabularyObject);
        providedSchemaId.ifPresent(id -> {
            JsonNodeUtil.validateIdField(id);
            unfinishedSchemas.put(id, metaSchemaData);
        });
        String absoluteUri = ctx.getAbsoluteUri(node);
        String finalUri = providedSchemaId.orElse(absoluteUri);
        Set<String> activeVocabularies = validateSchemaOrPostpone(node, metaSchemaUri, finalUri);

        if (providedSchemaId.isPresent()) {
            String idString = providedSchemaId.get();
            URI uri = ctx.getParentUri().resolve(idString);
            SchemaParsingContext newCtx = ctx.withParentUri(uri);
            List<EvaluatorWrapper> evaluators = parseEvaluators(newCtx, objectMap, node.getJsonPointer());
            schemaRegistry.registerIdentifiableSchema(newCtx, uri, node, evaluators, activeVocabularies);
        } else {
            schemaRegistry.registerSchema(ctx, node, parseEvaluators(ctx, objectMap, node.getJsonPointer()), activeVocabularies);
        }

        providedSchemaId.ifPresent(id -> {
            metaSchemaData.parsed();
            unfinishedSchemas.remove(id);
        });
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

    /* If meta-schema is the same as schema or is currently being processed, its validation needs to be postponed */
    private Set<String> validateSchemaOrPostpone(JsonNode node, String metaSchemaUri, String uri) {
        if (metaSchemaUri == null) {
            return dialect.getSupportedVocabularies();
        }
        if (!unfinishedSchemas.containsKey(metaSchemaUri)) {
            return metaSchemaValidator.validateSchema(this, metaSchemaUri, uri, node);
        }

        MetaSchemaData metaSchemaData = unfinishedSchemas.get(metaSchemaUri);
        metaSchemaData.callbacks.add(() -> metaSchemaValidator.validateSchema(this, metaSchemaUri, uri, node));
        return metaSchemaValidator.determineActiveVocabularies(metaSchemaData.vocabularyObject);
    }

    private static final class MetaSchemaData {
        private final Map<String, Boolean> vocabularyObject;
        private final ConcurrentLinkedDeque<Runnable> callbacks;

        private MetaSchemaData(Map<String, Boolean> vocabularyObject) {
            this.vocabularyObject = vocabularyObject;
            this.callbacks = new ConcurrentLinkedDeque<>();
        }

        void parsed() {
            callbacks.forEach(Runnable::run);
        }
    }
}

