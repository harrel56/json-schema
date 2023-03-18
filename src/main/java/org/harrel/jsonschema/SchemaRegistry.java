package org.harrel.jsonschema;

import java.net.URI;
import java.util.*;

public class SchemaRegistry {

    private final Map<String, Schema> schemas = new HashMap<>();
    private final Map<String, Schema> additionalSchemas = new HashMap<>();
    private final Map<String, Schema> dynamicSchemas = new HashMap<>();

    public Schema get(String uri) {
        return schemas.getOrDefault(uri, additionalSchemas.get(uri));
    }

    public Schema getDynamic(String anchor) {
        return dynamicSchemas.get(anchor);
    }

    public void registerSchema(SchemaParsingContext ctx, JsonNode schemaNode, List<ValidatorWrapper> validators) {
        Map<String, JsonNode> objectMap = schemaNode.asObject();
        Schema schema = new Schema(ctx.getParentUri(), ctx.getAbsoluteUri(schemaNode), validators);
        schemas.put(ctx.getAbsoluteUri(schemaNode), schema);
        registerAnchorsIfPresent(ctx, objectMap, schema);
    }

    public void registerIdentifiableSchema(SchemaParsingContext ctx, URI id, JsonNode schemaNode, List<ValidatorWrapper> validators) {
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
        Map<String, JsonNode> objectMap = schemaNode.asObject();
        Schema identifiableSchema = new Schema(ctx.getParentUri(), id.toString(), validators);
        schemas.put(id.toString(), identifiableSchema);
        schemas.put(absoluteUri, identifiableSchema);
        registerAnchorsIfPresent(ctx, objectMap, identifiableSchema);
    }

    private void registerAnchorsIfPresent(SchemaParsingContext ctx, Map<String, JsonNode> objectMap, Schema schema) {
        if (objectMap.containsKey("$anchor")) {
            String anchorFragment = "#" + objectMap.get("$anchor").asString();
            String anchoredUri = UriUtil.resolveUri(ctx.getParentUri(), anchorFragment);
            additionalSchemas.put(anchoredUri, schema);
        }
        if (objectMap.containsKey("$dynamicAnchor")) {
            String anchorFragment = "#" + objectMap.get("$dynamicAnchor").asString();
            dynamicSchemas.put(ctx.getParentUri().toString() + anchorFragment, schema);
        }
    }
}
