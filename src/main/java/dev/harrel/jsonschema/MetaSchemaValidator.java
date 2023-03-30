package dev.harrel.jsonschema;

import java.util.Optional;

final class MetaSchemaValidator {
    private static final String RESOLVING_ERROR_MSG = "Cannot resolve meta-schema [%s]";

    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;

    MetaSchemaValidator(SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        this.schemaRegistry = schemaRegistry;
        this.schemaResolver = schemaResolver;
    }

    void validateMetaSchema(JsonParser jsonParser, String metaSchemaUri, String schemaUri, JsonNode node) {
        if (metaSchemaUri == null || metaSchemaUri.equals(schemaUri)) {
            return;
        }
        Schema schema = resolveMetaSchema(jsonParser, metaSchemaUri)
                .orElseThrow(() -> new MetaSchemaResolvingException(RESOLVING_ERROR_MSG.formatted(metaSchemaUri)));
        ValidationContext newCtx = new ValidationContext(jsonParser, schemaRegistry, schemaResolver);
        if (!schema.validate(newCtx, node)) {
            throw new InvalidSchemaException("Schema [%s] failed to validate against meta-schema [%s]".formatted(schemaUri, metaSchemaUri));
        }
    }

    private Optional<Schema> resolveMetaSchema(JsonParser jsonParser, String uri) {
        return Optional.ofNullable(schemaRegistry.get(uri))
                .or(() -> Optional.ofNullable(schemaRegistry.getDynamic(uri)))
                .or(() -> resolveExternalSchema(jsonParser, uri));
    }

    private Optional<Schema> resolveExternalSchema(JsonParser jsonParser, String uri) {
        String baseUri = UriUtil.getUriWithoutFragment(uri);
        if (schemaRegistry.get(baseUri) != null) {
            throw new MetaSchemaResolvingException(RESOLVING_ERROR_MSG.formatted(uri));
        }
        String rawJson = schemaResolver.resolve(baseUri).orElseThrow(() -> new MetaSchemaResolvingException(RESOLVING_ERROR_MSG.formatted(uri)));
        try {
            jsonParser.parseRootSchema(baseUri, rawJson);
            return resolveMetaSchema(jsonParser, uri);
        } catch (Exception e) {
            throw new MetaSchemaResolvingException("Parsing meta-schema [%s] failed".formatted(uri), e);
        }
    }
}
