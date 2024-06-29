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
                        () -> Optional.ofNullable(dialect.getMetaSchema())
                )
                .map(URI::create)
                .orElse(null);
        Optional<URI> providedSchemaId = objectMapOptional
                .flatMap(obj -> JsonNodeUtil.getStringField(obj, Keyword.ID))
                .filter(JsonNodeUtil::validateIdField)
                .map(UriUtil::getUriWithoutFragment)
                .filter(id -> !baseUri.equals(id));

        MetaSchemaData metaSchemaData = new MetaSchemaData();
        unfinishedSchemas.put(baseUri, metaSchemaData);
        providedSchemaId.ifPresent(id -> unfinishedSchemas.put(id, metaSchemaData));

        URI finalUri = providedSchemaId.orElse(baseUri);
        MetaValidationData metaValidationData = validateAgainstMetaSchema(node, metaSchemaUri, finalUri.toString());

        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(metaValidationData, schemaRegistry, baseUri, emptyMap());
            List<EvaluatorWrapper> evaluators = singletonList(new EvaluatorWrapper(null, node, Schema.getBooleanEvaluator(node.asBoolean())));
            schemaRegistry.registerSchema(ctx, node, evaluators);
        } else if (objectMapOptional.isPresent()) {
            Map<String, JsonNode> objectMap = objectMapOptional.get();
            SchemaParsingContext ctx = new SchemaParsingContext(metaValidationData, schemaRegistry, finalUri, objectMap);
            List<EvaluatorWrapper> evaluators = parseEvaluators(ctx, objectMap, node.getJsonPointer());
            schemaRegistry.registerSchema(ctx, node, evaluators);
            providedSchemaId.ifPresent(id -> schemaRegistry.registerAlias(id, baseUri));
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
        URI metaSchemaUri = JsonNodeUtil.getStringField(objectMap, Keyword.SCHEMA)
                .map(URI::create)
                .orElse(null);
        Optional<URI> providedSchemaId = JsonNodeUtil.getStringField(objectMap, Keyword.ID)
                .filter(JsonNodeUtil::validateIdField)
                .map(URI::create);

        MetaSchemaData metaSchemaData = new MetaSchemaData();
        providedSchemaId.ifPresent(id -> unfinishedSchemas.put(id, metaSchemaData));

        String absoluteUri = ctx.getAbsoluteUri(node);
        String finalUri = providedSchemaId.map(URI::toString).orElse(absoluteUri);
        // todo validate embedded schemas only if $id is present
        MetaValidationData metaValidationData = validateAgainstMetaSchema(node, metaSchemaUri, finalUri);

        if (providedSchemaId.isPresent()) {
            URI idUri = providedSchemaId.get();
            URI uri = ctx.getParentUri().resolve(idUri);
            SchemaParsingContext newCtx = ctx.forChild(metaValidationData, objectMap, uri);
            List<EvaluatorWrapper> evaluators = parseEvaluators(newCtx, objectMap, node.getJsonPointer());
            schemaRegistry.registerEmbeddedSchema(newCtx, uri, node, evaluators);
            metaSchemaData.parsed();
            unfinishedSchemas.remove(idUri);
        } else {
            SchemaParsingContext newCtx = ctx.forChild(metaValidationData, objectMap);
            schemaRegistry.registerSchema(ctx, node, parseEvaluators(newCtx, objectMap, node.getJsonPointer()));
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

    /* If meta-schema is the same as schema or is currently being processed, its validation needs to be postponed */
    private MetaValidationData validateAgainstMetaSchema(JsonNode node, URI metaSchemaUri, String uri) {
        if (metaSchemaUri == null) {
            return new MetaValidationData(dialect.getSpecificationVersion(), dialect.getSpecificationVersion().getActiveVocabularies());
        }
        if (!unfinishedSchemas.containsKey(metaSchemaUri)) {
            return metaSchemaValidator.processMetaSchema(this, metaSchemaUri, uri, node);
        }

        SpecificationVersion schemaVersion = SpecificationVersion.fromUri(metaSchemaUri)
                .orElseThrow(() -> MetaSchemaResolvingException.recursiveFailure(metaSchemaUri.toString()));

        MetaSchemaData metaSchemaData = unfinishedSchemas.get(metaSchemaUri);
        metaSchemaData.callbacks.add(() -> metaSchemaValidator.processMetaSchema(this, metaSchemaUri, uri, node));
        return new MetaValidationData(schemaVersion, schemaVersion.getActiveVocabularies());
    }

    private EvaluatorFactory createEvaluatorFactory(SchemaParsingContext ctx) {
        if (evaluatorFactory != null) {
            return EvaluatorFactory.compose(evaluatorFactory, ctx.getMetaValidationData().specificationVersion.getEvaluatorFactory());
        } else {
            return ctx.getMetaValidationData().specificationVersion.getEvaluatorFactory();
        }
    }

    // todo is this class necessary?
    private static final class MetaSchemaData {
        private final List<Runnable> callbacks = new ArrayList<>();

        void parsed() {
            /* old good for loop to avoid ConcurrentModificationException */
            for (int i = 0; i < callbacks.size(); i++) {
                callbacks.get(i).run();
            }
        }
    }
}

