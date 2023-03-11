package org.harrel.jsonschema;

import java.net.URI;
import java.util.*;

public class SchemaRegistry {

    private final Map<String, Schema> schemas = new HashMap<>();
    private final Map<String, Schema> additionalSchemas = new HashMap<>();
    private final Map<String, Schema> dynamicAnchors = new HashMap<>();

    public Schema get(String uri) {
        Schema schema = schemas.getOrDefault(uri, additionalSchemas.get(uri));
        if (schema != null) {
            schema.getDynamicAnchor()
                    .filter(dynamicAnchor -> !dynamicAnchors.containsKey(dynamicAnchor))
                    .ifPresent(dynamicAnchor -> dynamicAnchors.put(dynamicAnchor, schema));
        }
        return schema;
    }

    public Schema getByDynamicAnchor(String anchor) {
        return dynamicAnchors.get(anchor);
    }

    public void registerSchema(SchemaParsingContext ctx, JsonNode schemaNode, List<ValidatorDelegate> validators) {
        Map<String, JsonNode> objectMap = schemaNode.asObject();
        Schema schema = new Schema(validators, getDynamicAnchor(objectMap));
        put(schemas, ctx.getAbsoluteUri(schemaNode), schema);
        registerAnchorsIfPresent(ctx, objectMap, schema);
    }

    public void registerIdentifiableSchema(SchemaParsingContext ctx, URI id, JsonNode schemaNode, List<ValidatorDelegate> validators) {
        String absoluteUri = ctx.getAbsoluteUri(schemaNode);
        schemas.entrySet().stream()
                .filter(e -> e.getKey().startsWith(absoluteUri))
                .forEach(e -> {
                    /* Special case for root json pointer, because it ends with slash */
                    int normalizedUriSize = absoluteUri.endsWith("/") ? absoluteUri.length() - 1 : absoluteUri.length();
                    String newJsonPointer = e.getKey().substring(normalizedUriSize);
                    String newUri = id.toString() + "#" + newJsonPointer;
                    put(additionalSchemas, newUri, e.getValue());
                });
        Map<String, JsonNode> objectMap = schemaNode.asObject();
        IdentifiableSchema identifiableSchema = new IdentifiableSchema(id, validators, getDynamicAnchor(objectMap));
        put(schemas, id.toString(), identifiableSchema);
        put(schemas, absoluteUri, identifiableSchema);
        registerAnchorsIfPresent(ctx, objectMap, identifiableSchema);
    }

    private String getDynamicAnchor(Map<String, JsonNode> objectMap) {
        return Optional.ofNullable(objectMap.get("$dynamicAnchor"))
                .map(JsonNode::asString)
                .orElse(null);
    }

    private void registerAnchorsIfPresent(SchemaParsingContext ctx, Map<String, JsonNode> objectMap, Schema schema) {
        if (objectMap.containsKey("$anchor")) {
            String anchorFragment = "#" + objectMap.get("$anchor").asString();
            String anchoredUri = UriUtil.resolveUri(ctx.getParentUri(), anchorFragment);
            put(additionalSchemas, anchoredUri, schema);
        }
        if (objectMap.containsKey("$dynamicAnchor")) {
            String anchorFragment = "#" + objectMap.get("$dynamicAnchor").asString();
            String anchoredUri = UriUtil.resolveUri(ctx.getParentUri(), anchorFragment);
            put(additionalSchemas, anchoredUri, schema);
        }
    }

    private void put(Map<String, Schema> map, String key, Schema value) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate schema registration, uri=" + key);
        }
        map.put(key, value);
    }
}
