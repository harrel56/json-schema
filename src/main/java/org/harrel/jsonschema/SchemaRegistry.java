package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.Validator;

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

    public void registerSchema(SchemaParsingContext ctx, JsonNode schemaNode, List<Validator> validators) {
        put(schemas, ctx.getAbsoluteUri(schemaNode), new Schema(validators));
    }

    public void registerIdentifiableSchema(SchemaParsingContext ctx, URI id, JsonNode schemaNode, List<Validator> validators) {
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
    }

    private void put(Map<String, Schema> map, String key, Schema value) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate schema registration, uri=" + key);
        }
        map.put(key, value);
    }
}
