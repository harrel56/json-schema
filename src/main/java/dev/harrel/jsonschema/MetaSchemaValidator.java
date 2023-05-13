package dev.harrel.jsonschema;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

final class MetaSchemaValidator {
    private static final String RESOLVING_ERROR_MSG = "Cannot resolve meta-schema [%s]";

    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaRegistry schemaRegistry;
    private final SchemaResolver schemaResolver;

    MetaSchemaValidator(JsonNodeFactory jsonNodeFactory, SchemaRegistry schemaRegistry, SchemaResolver schemaResolver) {
        this.jsonNodeFactory = Objects.requireNonNull(jsonNodeFactory);
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry);
        this.schemaResolver = Objects.requireNonNull(schemaResolver);
    }

    void validateMetaSchema(JsonParser jsonParser, String metaSchemaUri, String schemaUri, JsonNode node) {
        if (metaSchemaUri == null || metaSchemaUri.equals(schemaUri)) {
            return;
        }
        Schema schema = resolveMetaSchema(jsonParser, metaSchemaUri)
                .orElseThrow(() -> new MetaSchemaResolvingException(RESOLVING_ERROR_MSG.formatted(metaSchemaUri)));
        EvaluationContext ctx = new EvaluationContext(jsonNodeFactory, jsonParser, schemaRegistry, schemaResolver);
        if (!ctx.validateAgainstSchema(schema, node)) {
            throw new InvalidSchemaException("Schema [%s] failed to validate against meta-schema [%s]".formatted(schemaUri, metaSchemaUri));
        }
    }

    private Optional<Schema> resolveMetaSchema(JsonParser jsonParser, String uri) {
        return Optional.ofNullable(schemaRegistry.get(uri))
                .or(() -> Optional.ofNullable(schemaRegistry.getDynamic(uri)))
                .or(() -> resolveExternalSchema(jsonParser, uri));
    }

    // TODO doubled optional?
    private Optional<Schema> resolveExternalSchema(JsonParser jsonParser, String uri) {
        String baseUri = UriUtil.getUriWithoutFragment(uri);
        if (schemaRegistry.get(baseUri) != null) {
            throw new MetaSchemaResolvingException(RESOLVING_ERROR_MSG.formatted(uri));
        }
        return schemaResolver.resolve(baseUri)
                .toJsonNode(jsonNodeFactory)
                .map(node -> {
                    try {
                        jsonParser.parseRootSchema(URI.create(baseUri), node);
                        return resolveMetaSchema(jsonParser, uri);
                    } catch (Exception e) {
                        throw new MetaSchemaResolvingException("Parsing meta-schema [%s] failed".formatted(uri), e);
                    }
                })
                .orElseThrow(() -> new MetaSchemaResolvingException(RESOLVING_ERROR_MSG.formatted(uri)));

    }
}
