package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

final class JsonParser {
    private final Map<URI, Dialect> dialects;
    private final Dialect defaultDialect;
    private final EvaluatorFactory evaluatorFactory;
    private final SchemaRegistry schemaRegistry;
    private final MetaSchemaValidator metaSchemaValidator;
    private final Map<URI, UnfinishedSchema> unfinishedSchemas = new HashMap<>();

    JsonParser(Map<URI, Dialect> dialects,
               Dialect defaultDialect,
               EvaluatorFactory evaluatorFactory,
               SchemaRegistry schemaRegistry,
               MetaSchemaValidator metaSchemaValidator) {
        this.dialects = Objects.requireNonNull(dialects);
        this.defaultDialect = Objects.requireNonNull(defaultDialect);
        this.evaluatorFactory = evaluatorFactory;
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.metaSchemaValidator = Objects.requireNonNull(metaSchemaValidator);
    }

    synchronized URI parseRootSchema(URI baseUri, JsonNode node) {
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
                        () -> Optional.ofNullable(defaultDialect.getMetaSchema())
                )
                .map(URI::create)
                .orElse(null);
        Optional<URI> providedSchemaId = objectMapOptional
                .flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.ID))
                .filter(JsonNodeUtil::validateIdField)
                .map(UriUtil::getUriWithoutFragment)
                .filter(id -> !baseUri.equals(id));

        UnfinishedSchema unfinishedSchema = new UnfinishedSchema();
        unfinishedSchemas.put(baseUri, unfinishedSchema);
        providedSchemaId.ifPresent(id -> unfinishedSchemas.put(id, unfinishedSchema));

        URI finalUri = providedSchemaId.orElse(baseUri);
        MetaSchemaData metaSchemaData = validateAgainstMetaSchema(node, metaSchemaUri, finalUri.toString());

        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(metaSchemaData, schemaRegistry, baseUri, emptyMap());
            List<EvaluatorWrapper> evaluators = singletonList(new EvaluatorWrapper(null, node, Schema.getBooleanEvaluator(node.asBoolean())));
            schemaRegistry.registerSchema(ctx, node, evaluators);
        } else if (objectMapOptional.isPresent()) {
            Map<String, JsonNode> objectMap = objectMapOptional.get();
            SchemaParsingContext ctx = new SchemaParsingContext(metaSchemaData, schemaRegistry, finalUri, objectMap);
            List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
            schemaRegistry.registerSchema(ctx, node, evaluators);
            providedSchemaId.ifPresent(id -> schemaRegistry.registerAlias(id, baseUri));
        }

        unfinishedSchema.parsed();
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
        Evaluator booleanEvaluator = Schema.getBooleanEvaluator(node.asBoolean());
        List<EvaluatorWrapper> evaluators = singletonList(new EvaluatorWrapper(null, node, booleanEvaluator));
        schemaRegistry.registerSchema(ctx, node, evaluators);
    }

    private void parseArray(SchemaParsingContext ctx, JsonNode node) {
        for (JsonNode element : node.asArray()) {
            parseNode(ctx, element);
        }
    }

    private void parseObject(SchemaParsingContext ctx, JsonNode node) {
        Map<String, JsonNode> objectMap = node.asObject();
        Optional<URI> providedSchemaId = JsonNodeUtil.getStringField(objectMap, Keyword.ID)
                .filter(JsonNodeUtil::validateIdField)
                .map(URI::create);

        if (!providedSchemaId.isPresent()) {
            SchemaParsingContext newCtx = ctx.forChild(objectMap);
            schemaRegistry.registerSchema(ctx, node, parseEvaluators(newCtx, objectMap, node.getJsonPointer()));
        } else {
            /* Embedded schema handling */
            URI idUri = providedSchemaId.get();
            UnfinishedSchema unfinishedSchema = new UnfinishedSchema();
            unfinishedSchemas.put(idUri, unfinishedSchema);
            MetaSchemaData metaSchemaData = JsonNodeUtil.getStringField(objectMap, Keyword.SCHEMA)
                    .map(URI::create)
                    .map(metaSchemaUri -> validateAgainstMetaSchema(node, metaSchemaUri, idUri.toString()))
                    .orElse(ctx.getMetaValidationData());

            URI uri = ctx.getParentUri().resolve(idUri);
            SchemaParsingContext newCtx = ctx.forChild(metaSchemaData, objectMap, uri);
            List<EvaluatorWrapper> evaluators = parseEvaluators(newCtx, objectMap, node.getJsonPointer());
            schemaRegistry.registerEmbeddedSchema(newCtx, uri, node, evaluators);
            unfinishedSchema.parsed();
            unfinishedSchemas.remove(idUri);
        }
    }

    private List<EvaluatorWrapper> parseEvaluators(SchemaParsingContext ctx, Map<String, JsonNode> object, String objectPath) {
        List<EvaluatorWrapper> evaluators = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : object.entrySet()) {
            createEvaluatorFactory(ctx).create(ctx, entry.getKey(), entry.getValue())
                    .map(evaluator -> new EvaluatorWrapper(entry.getKey(), entry.getValue(), evaluator))
                    .ifPresent(evaluators::add);
            parseNode(ctx, entry.getValue());
        }
        if (evaluators.isEmpty()) {
            evaluators.add(new EvaluatorWrapper(null, objectPath, Schema.getBooleanEvaluator(true)));
        }
        return evaluators;
    }

    private MetaSchemaData validateAgainstMetaSchema(JsonNode node, URI metaSchemaUri, String uri) {
        Dialect dialect = dialects.get(metaSchemaUri);
        UnfinishedSchema unfinishedSchema = unfinishedSchemas.get(metaSchemaUri);
        /* If meta-schema is the same as schema or is currently being processed, its validation needs to be postponed */
        if (unfinishedSchema != null) {
            if (dialect == null) {
                throw MetaSchemaResolvingException.recursiveFailure(metaSchemaUri.toString());
            }
            unfinishedSchema.callbacks.add(() -> metaSchemaValidator.processMetaSchema(this, metaSchemaUri, uri, node));
            return new MetaSchemaData(dialect);
        }

        MetaSchemaData metaSchemaData =  metaSchemaValidator.processMetaSchema(this, metaSchemaUri, uri, node);
        if (dialect == null) {
            return metaSchemaData;
        }

        /* If this is a registered dialect and meta-schema defines no vocabs, use vocabs from dialect */
        if (metaSchemaData.vocabularyObject == null) {
            return new MetaSchemaData(dialect, dialect.getDefaultVocabularyObject(), dialect.getDefaultVocabularyObject().keySet());
        } else {
            return new MetaSchemaData(dialect, metaSchemaData.vocabularyObject, metaSchemaData.activeVocabularies);
        }
    }

    private EvaluatorFactory createEvaluatorFactory(SchemaParsingContext ctx) {
        if (evaluatorFactory != null) {
            return EvaluatorFactory.compose(evaluatorFactory, ctx.getMetaValidationData().dialect.getEvaluatorFactory());
        } else {
            return ctx.getMetaValidationData().dialect.getEvaluatorFactory();
        }
    }

    private static final class UnfinishedSchema {
        private final List<Runnable> callbacks = new ArrayList<>();

        void parsed() {
            /* old good for loop to avoid ConcurrentModificationException */
            for (int i = 0; i < callbacks.size(); i++) {
                callbacks.get(i).run();
            }
        }
    }
}

