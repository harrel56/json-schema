package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

final class JsonParser {
    private final Map<URI, Dialect> dialects;
    private final Dialect defaultDialect;
    private final EvaluatorFactory evaluatorFactory;
    private final SchemaRegistry schemaRegistry;
    private final MetaSchemaValidator metaSchemaValidator;
    private final boolean disabledSchemaValidation;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<URI, UnfinishedSchema> unfinishedSchemas = new HashMap<>();

    JsonParser(Map<URI, Dialect> dialects,
               Dialect defaultDialect,
               EvaluatorFactory evaluatorFactory,
               SchemaRegistry schemaRegistry,
               MetaSchemaValidator metaSchemaValidator,
               boolean disabledSchemaValidation) {
        this.dialects = Objects.requireNonNull(dialects);
        this.defaultDialect = Objects.requireNonNull(defaultDialect);
        this.evaluatorFactory = evaluatorFactory;
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.metaSchemaValidator = Objects.requireNonNull(metaSchemaValidator);
        this.disabledSchemaValidation = disabledSchemaValidation;
    }

    URI parseRootSchema(URI baseUri, JsonNode node) {
        lock.lock();
        try {
            SchemaRegistry.State snapshot = schemaRegistry.createSnapshot();
            try {
                return parseRootSchemaInternal(UriUtil.getUriWithoutFragment(baseUri), node);
            } catch (RuntimeException e) {
                schemaRegistry.restoreSnapshot(snapshot);
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    private URI parseRootSchemaInternal(URI baseUri, JsonNode node) {
        Optional<Map<String, JsonNode>> objectMapOptional = JsonNodeUtil.getAsObject(node);
        URI metaSchemaUri = Optional.ofNullable(
                        objectMapOptional.flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.SCHEMA))
                                .orElseGet(defaultDialect::getMetaSchema)
                )
                .map(UriUtil::removeEmptyFragment)
                .orElse(null);
        Optional<String> idField = objectMapOptional.flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.ID));
        Optional<URI> providedSchemaId = idField
                .map(UriUtil::getUriWithoutFragment)
                .filter(id -> !baseUri.equals(id));

        UnfinishedSchema unfinishedSchema = new UnfinishedSchema();
        unfinishedSchemas.put(baseUri, unfinishedSchema);
        providedSchemaId.ifPresent(id -> unfinishedSchemas.put(id, unfinishedSchema));

        SpecificationVersion specVersion = resolveSpecVersion(metaSchemaUri);
        if (specVersion.getOrder() <= SpecificationVersion.DRAFT4.getOrder()) {
            providedSchemaId.ifPresent(unfinishedSchemas::remove);
            idField = objectMapOptional.flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.LEGACY_ID));
            providedSchemaId = idField
                    .map(UriUtil::getUriWithoutFragment)
                    .filter(id -> !baseUri.equals(id));
            providedSchemaId.ifPresent(id -> unfinishedSchemas.put(id, unfinishedSchema));
        }

        URI finalUri = providedSchemaId.orElse(baseUri);
        MetaSchemaData metaSchemaData = validateAgainstMetaSchema(node, metaSchemaUri, finalUri.toString());

        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(metaSchemaData, baseUri, schemaRegistry, emptyMap());
            List<EvaluatorWrapper> evaluators = singletonList(new EvaluatorWrapper(null, node, Schema.getBooleanEvaluator(node.asBoolean())));
            schemaRegistry.registerSchema(ctx, node, evaluators);
        } else if (objectMapOptional.isPresent()) {
            Map<String, JsonNode> objectMap = objectMapOptional.get();
            SchemaParsingContext ctx = new SchemaParsingContext(metaSchemaData, finalUri, schemaRegistry, objectMap);
            idField.ifPresent(id -> validateIdField(ctx, id));
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
        SpecificationVersion specVersion = JsonNodeUtil.getStringField(objectMap, Keyword.SCHEMA)
                .map(UriUtil::removeEmptyFragment)
                .map(dialects::get)
                .map(Dialect::getSpecificationVersion)
                .orElse(ctx.getMetaValidationData().dialect.getSpecificationVersion());
        Optional<String> idField = JsonNodeUtil.getStringField(objectMap, Keyword.getIdKeyword(specVersion));
        boolean isEmbeddedSchema = idField
                .map(id -> !id.startsWith("#") || specVersion.getOrder() > SpecificationVersion.DRAFT7.getOrder())
                .orElse(false);

        if (!isEmbeddedSchema) {
            SchemaParsingContext newCtx = ctx.forChild(objectMap);
            schemaRegistry.registerSchema(ctx, node, parseEvaluators(newCtx, objectMap, node.getJsonPointer()));
        } else {
            /* Embedded schema handling */
            String idString = idField.get();
            URI idUri = URI.create(idString);
            UnfinishedSchema unfinishedSchema = new UnfinishedSchema();
            unfinishedSchemas.put(idUri, unfinishedSchema);
            MetaSchemaData metaSchemaData = JsonNodeUtil.getStringField(objectMap, Keyword.SCHEMA)
                    .map(UriUtil::removeEmptyFragment)
                    .map(metaSchemaUri -> validateAgainstMetaSchema(node, metaSchemaUri, idUri.toString()))
                    .orElse(ctx.getMetaValidationData());

            URI uri = ctx.getParentUri().resolve(idUri);
            SchemaParsingContext newCtx = ctx.forChild(metaSchemaData, objectMap, uri);
            validateIdField(newCtx, idString);
            List<EvaluatorWrapper> evaluators = parseEvaluators(newCtx, objectMap, node.getJsonPointer());
            schemaRegistry.registerEmbeddedSchema(newCtx, uri, node, evaluators);
            unfinishedSchema.parsed();
            unfinishedSchemas.remove(idUri);
        }
    }

    private List<EvaluatorWrapper> parseEvaluators(SchemaParsingContext ctx, Map<String, JsonNode> object, String objectPath) {
        List<EvaluatorWrapper> evaluators = new ArrayList<>();
        JsonNode refOverride = null;
        /* until draft2019, $ref must ignore sibling keywords */
        if (ctx.getSpecificationVersion().getOrder() <= SpecificationVersion.DRAFT7.getOrder()) {
            refOverride = object.get(Keyword.REF);
        }

        for (Map.Entry<String, JsonNode> entry : object.entrySet()) {
            if (refOverride == null || entry.getValue() == refOverride) {
                createEvaluatorFactory(ctx).create(ctx, entry.getKey(), entry.getValue())
                        .map(evaluator -> new EvaluatorWrapper(entry.getKey(), entry.getValue(), evaluator))
                        .ifPresent(evaluators::add);
            }
            parseNode(ctx, entry.getValue());
        }
        if (evaluators.isEmpty()) {
            evaluators.add(new EvaluatorWrapper(null, objectPath, Schema.getBooleanEvaluator(true)));
        }
        return evaluators;
    }

    private MetaSchemaData validateAgainstMetaSchema(JsonNode node, URI metaSchemaUri, String uri) {
        MetaSchemaData data = resolveMetaSchemaData(node, metaSchemaUri, uri);
        new VocabularyValidator().validateVocabularies(data.dialect, data.vocabularyObject);
        return data;
    }

    private SpecificationVersion resolveSpecVersion(URI metaSchemaUri) {
        // todo can be removed if Dialect.getMetaSchema() is guaranteed non-null
        if (metaSchemaUri == null) {
            return defaultDialect.getSpecificationVersion();
        }
        Dialect dialect = dialects.get(metaSchemaUri);
        if (dialect == null) {
            if (disabledSchemaValidation) {
                dialect = defaultDialect;
            } else {
                if (unfinishedSchemas.containsKey(metaSchemaUri)) {
                    throw MetaSchemaResolvingException.recursiveFailure(metaSchemaUri.toString());
                }
                dialect = metaSchemaValidator.resolveMetaSchema(this, metaSchemaUri).getMetaValidationData().dialect;
            }
        }
        return dialect.getSpecificationVersion();
    }

    private MetaSchemaData resolveMetaSchemaData(JsonNode node, URI metaSchemaUri, String uri) {
        if (disabledSchemaValidation) {
            return new MetaSchemaData(dialects.getOrDefault(metaSchemaUri, defaultDialect));
        }

        Dialect dialect = dialects.get(metaSchemaUri);
        UnfinishedSchema unfinishedSchema = unfinishedSchemas.get(metaSchemaUri);
        /* If meta-schema is the same as schema or is currently being processed, its validation needs to be postponed */
        if (unfinishedSchema != null) {
            if (dialect == null) {
                throw MetaSchemaResolvingException.recursiveFailure(metaSchemaUri.toString());
            }
            unfinishedSchema.callbacks.add(() -> metaSchemaValidator.validateSchema(this, metaSchemaUri, uri, node));
            return new MetaSchemaData(dialect);
        }

        MetaSchemaData metaSchemaData =  metaSchemaValidator.validateSchema(this, metaSchemaUri, uri, node);
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

    private static void validateIdField(SchemaParsingContext ctx, String id) {
        URI uri = URI.create(id);
        if (ctx.getSpecificationVersion().getOrder() > SpecificationVersion.DRAFT7.getOrder()) {
            if (uri.getRawFragment() != null && !uri.getRawFragment().isEmpty()) {
                throw new IllegalArgumentException(String.format("$id [%s] cannot contain non-empty fragments", id));
            }
        } else {
            if (uri.getRawFragment() != null && uri.getRawFragment().startsWith("/")) {
                throw new IllegalArgumentException(String.format("$id [%s] cannot contain fragments starting with '/'", id));
            }
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

