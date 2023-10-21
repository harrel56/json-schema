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
    private final Map<URI, MetaSchemaData> unfinishedSchemas = new HashMap<>();

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
        SchemaRegistry.State snapshot = schemaRegistry.createSnapshot();
        try {
            return parseRootSchemaInternal(UriUtil.getUriWithoutFragment(baseUri), node);
        } catch (RuntimeException e) {
            schemaRegistry.restoreSnapshot(snapshot);
            throw e;
        }
    }

    private URI parseRootSchemaInternal(URI baseUri, JsonNode node) {
        Optional<Map<String, JsonNode>> objectMapOptional = JsonNodeUtil.getAsObject(node);
        URI metaSchemaUri = OptionalUtil.firstPresent(
                        () -> objectMapOptional.flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.SCHEMA)),
                        () -> Optional.ofNullable(dialect.getMetaSchema())
                )
                .map(URI::create)
                .orElse(null);
        Optional<URI> providedSchemaId = objectMapOptional
                .flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.ID))
                .filter(JsonNodeUtil::validateIdField)
                .map(UriUtil::getUriWithoutFragment)
                .filter(id -> !baseUri.equals(id));
        Map<String, Boolean> vocabulariesObject = objectMapOptional
                .flatMap(JsonNodeUtil::getVocabulariesObject)
                .orElse(dialect.getDefaultVocabularyObject());

        MetaSchemaData metaSchemaData = new MetaSchemaData(vocabulariesObject);
        unfinishedSchemas.put(baseUri, metaSchemaData);
        providedSchemaId.ifPresent(id -> unfinishedSchemas.put(id, metaSchemaData));

        URI finalUri = providedSchemaId.orElse(baseUri);
        Set<String> activeVocabularies = validateSchemaOrPostpone(node, metaSchemaUri, finalUri.toString());

        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, baseUri, emptyMap());
            List<EvaluatorWrapper> evaluators = singletonList(new EvaluatorWrapper(null, node, Schema.getBooleanEvaluator(node.asBoolean())));
            schemaRegistry.registerIdentifiableSchema(ctx, node, evaluators, activeVocabularies);
        } else if (objectMapOptional.isPresent()) {
            Map<String, JsonNode> objectMap = objectMapOptional.get();
            if (providedSchemaId.isPresent()) {
                URI idUri = providedSchemaId.get();
                SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, idUri, objectMap);
                List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
                schemaRegistry.registerIdentifiableSchema(ctx, node, evaluators, activeVocabularies);
                schemaRegistry.registerAlias(idUri, baseUri);
            } else {
                SchemaParsingContext ctx = new SchemaParsingContext(dialect, schemaRegistry, baseUri, objectMap);
                List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
                schemaRegistry.registerIdentifiableSchema(ctx, node, evaluators, activeVocabularies);
            }
        }

        metaSchemaData.parsed();
        unfinishedSchemas.remove(baseUri);
        providedSchemaId.ifPresent(unfinishedSchemas::remove);

        return finalUri;
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
        URI metaSchemaUri = JsonNodeUtil.getStringField(objectMap, Keyword.SCHEMA)
                .map(URI::create)
                .orElse(null);
        Optional<URI> providedSchemaId = JsonNodeUtil.getStringField(objectMap, Keyword.ID)
                .filter(JsonNodeUtil::validateIdField)
                .map(URI::create);
        Map<String, Boolean> vocabularyObject = JsonNodeUtil.getVocabulariesObject(objectMap)
                .orElse(dialect.getDefaultVocabularyObject());
        MetaSchemaData metaSchemaData = new MetaSchemaData(vocabularyObject);
        providedSchemaId.ifPresent(id -> unfinishedSchemas.put(id, metaSchemaData));

        String absoluteUri = ctx.getAbsoluteUri(node);
        String finalUri = providedSchemaId.map(URI::toString).orElse(absoluteUri);
        Set<String> activeVocabularies = validateSchemaOrPostpone(node, metaSchemaUri, finalUri);

        if (providedSchemaId.isPresent()) {
            URI idUri = providedSchemaId.get();
            URI uri = ctx.getParentUri().resolve(idUri);
            SchemaParsingContext newCtx = ctx.withParentUri(uri);
            List<EvaluatorWrapper> evaluators = parseEvaluators(newCtx, objectMap, node.getJsonPointer());
            schemaRegistry.registerEmbeddedIdentifiableSchema(newCtx, uri, node, evaluators, activeVocabularies);
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
    private Set<String> validateSchemaOrPostpone(JsonNode node, URI metaSchemaUri, String uri) {
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
        private final List<Runnable> callbacks;

        private MetaSchemaData(Map<String, Boolean> vocabularyObject) {
            this.vocabularyObject = vocabularyObject;
            this.callbacks = new ArrayList<>();
        }

        void parsed() {
            /* old good for loop to avoid ConcurrentModificationException */
            for (int i = 0; i < callbacks.size(); i++) {
                callbacks.get(i).run();
            }
        }
    }
}

