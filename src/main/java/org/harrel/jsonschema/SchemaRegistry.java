package org.harrel.jsonschema;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaRegistry {

    private final Map<String, Schema> schemas = new HashMap<>();
    private final Map<String, Schema> additionalSchemas = new HashMap<>();

    public Map<String, Schema> asSchemaCache() {
        HashMap<String, Schema> cache = new HashMap<>(schemas);
        cache.putAll(additionalSchemas);
        return Collections.unmodifiableMap(cache);
    }

    public void registerSchema(SchemaParsingContext ctx, JsonNode schemaNode, List<ValidatorDelegate> validators) {
        Schema schema = new Schema(validators);
        put(schemas, ctx.getAbsoluteUri(schemaNode), schema);
        registerAnchorIfPresent(ctx, schemaNode, schema);
    }

    public void registerIdentifiableSchema(SchemaParsingContext ctx, URI id, JsonNode schemaNode, List<ValidatorDelegate> validators) {
        String absoluteUri = ctx.getAbsoluteUri(schemaNode);
        schemas.entrySet().stream()
                .filter(e -> e.getKey().startsWith(absoluteUri))
                .forEach(e -> {
                    String newJsonPointer = e.getKey().substring(absoluteUri.length());
                    String newUri = id.toString() + "#" + newJsonPointer;
                    put(additionalSchemas, newUri, e.getValue());
                });

        IdentifiableSchema identifiableSchema = new IdentifiableSchema(id, validators);
        put(schemas, id.toString(), identifiableSchema);
        put(schemas, absoluteUri, identifiableSchema);
        registerAnchorIfPresent(ctx, schemaNode, identifiableSchema);
    }

    private void registerAnchorIfPresent(SchemaParsingContext ctx, JsonNode node, Schema schema) {
        Map<String, JsonNode> objectMap = node.asObject();
        if (objectMap.containsKey("$anchor")) {
            String anchorFragment = "#" + objectMap.get("$anchor").asString();
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
