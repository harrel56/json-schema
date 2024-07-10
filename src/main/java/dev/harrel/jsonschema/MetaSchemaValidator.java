package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

class MetaSchemaData {
    final Dialect dialect;
    final Map<String, Boolean> vocabularyObject;
    final Set<String> activeVocabularies;

    MetaSchemaData(Dialect dialect, Map<String, Boolean> vocabularyObject, Set<String> activeVocabularies) {
        this.dialect = dialect;
        this.vocabularyObject = vocabularyObject;
        this.activeVocabularies = activeVocabularies;
    }

    MetaSchemaData(Dialect dialect) {
        this(dialect, dialect.getDefaultVocabularyObject(), dialect.getDefaultVocabularyObject().keySet());
    }
}

class MetaSchemaValidator {
    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;

    MetaSchemaValidator(JsonNodeFactory jsonNodeFactory, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
    }

    MetaSchemaData validateSchema(JsonParser jsonParser, URI metaSchemaUri, String schemaUri, JsonNode node) {
        Objects.requireNonNull(metaSchemaUri);
        Schema schema = resolveMetaSchema(jsonParser, metaSchemaUri);
        EvaluationContext ctx = new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver);
        if (!ctx.validateAgainstSchema(schema, node)) {
            throw new InvalidSchemaException(String.format("Schema [%s] failed to validate against meta-schema [%s]", schemaUri, metaSchemaUri),
                    new Validator.Result(false, ctx).getErrors());
        }
        return schema.getMetaValidationData();
    }

    private Schema resolveMetaSchema(JsonParser jsonParser, URI uri) {
        CompoundUri compoundUri = CompoundUri.fromString(uri.toString());
        return OptionalUtil.firstPresent(
                () -> Optional.ofNullable(schemaRegistry.get(compoundUri)),
                () -> Optional.ofNullable(schemaRegistry.getDynamic(compoundUri))
        ).orElseGet(() -> resolveExternalSchema(jsonParser, uri));
    }

    private Schema resolveExternalSchema(JsonParser jsonParser, URI uri) {
        URI baseUri = UriUtil.getUriWithoutFragment(uri);
        if (schemaRegistry.get(baseUri) != null) {
            throw MetaSchemaResolvingException.resolvingFailure(uri.toString());
        }
        SchemaResolver.Result result = schemaResolver.resolve(baseUri.toString());
        if (result.isEmpty()) {
            throw MetaSchemaResolvingException.resolvingFailure(uri.toString());
        }
        try {
            result.toJsonNode(jsonNodeFactory).ifPresent(node -> jsonParser.parseRootSchema(baseUri, node));
        } catch (Exception e) {
            throw MetaSchemaResolvingException.parsingFailure(uri.toString(), e);
        }
        return resolveMetaSchema(jsonParser, uri);
    }
}
