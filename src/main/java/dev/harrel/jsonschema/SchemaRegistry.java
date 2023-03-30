package dev.harrel.jsonschema;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SchemaRegistry {

    private final Map<String, Schema> schemas = new HashMap<>();
    private final Map<String, Schema> additionalSchemas = new HashMap<>();
    private final Map<String, Schema> dynamicSchemas = new HashMap<>();

    Schema get(String uri) {
        return schemas.getOrDefault(uri, additionalSchemas.get(uri));
    }

    Schema getDynamic(String anchor) {
        return dynamicSchemas.get(anchor);
    }

    void registerSchema(SchemaParsingContext ctx, JsonNode schemaNode, List<ValidatorWrapper> validators) {
        Schema schema = new Schema(ctx.getParentUri(), ctx.getAbsoluteUri(schemaNode), validators);
        schemas.put(ctx.getAbsoluteUri(schemaNode), schema);
        registerAnchorsIfPresent(ctx, schemaNode, schema);
    }

    void registerIdentifiableSchema(SchemaParsingContext ctx, URI id, JsonNode schemaNode, List<ValidatorWrapper> validators) {
        String absoluteUri = ctx.getAbsoluteUri(schemaNode);
        schemas.entrySet().stream()
                .filter(e -> e.getKey().startsWith(absoluteUri))
                .forEach(e -> {
                    /* Special case for root json pointer, because it ends with slash */
                    int normalizedUriSize = absoluteUri.endsWith("/") ? absoluteUri.length() - 1 : absoluteUri.length();
                    String newJsonPointer = e.getKey().substring(normalizedUriSize);
                    String newUri = id.toString() + "#" + newJsonPointer;
                    additionalSchemas.put(newUri, e.getValue());
                });
        Schema identifiableSchema = new Schema(ctx.getParentUri(), id.toString(), validators);
        schemas.put(id.toString(), identifiableSchema);
        schemas.put(absoluteUri, identifiableSchema);
        registerAnchorsIfPresent(ctx, schemaNode, identifiableSchema);
    }

    private void registerAnchorsIfPresent(SchemaParsingContext ctx, JsonNode schemaNode, Schema schema) {
        if (!schemaNode.isObject()) {
            return;
        }
        Map<String, JsonNode> objectMap = schemaNode.asObject();
        JsonNode anchorNode = objectMap.get(Keyword.ANCHOR);
        if (anchorNode != null && anchorNode.isString()) {
            String anchorFragment = "#" + anchorNode.asString();
            String anchoredUri = UriUtil.resolveUri(ctx.getParentUri(), anchorFragment);
            additionalSchemas.put(anchoredUri, schema);
        }
        JsonNode dynamicAnchorNode = objectMap.get(Keyword.DYNAMIC_ANCHOR);
        if (dynamicAnchorNode != null && dynamicAnchorNode.isString()) {
            String anchorFragment = "#" + dynamicAnchorNode.asString();
            dynamicSchemas.put(ctx.getParentUri().toString() + anchorFragment, schema);
        }
    }
}
