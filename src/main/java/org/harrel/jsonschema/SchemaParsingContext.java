package org.harrel.jsonschema;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SchemaParsingContext {
    private final URI baseUri;
    private final Map<String, Schema> schemaCache = new HashMap<>();
    private final Map<URI, URI> anchors = new HashMap<>();

    public SchemaParsingContext(URI baseUri) {
        this.baseUri = baseUri;
    }

    public String getAbsoluteUri(JsonNode node) {
        return getAbsoluteUri(node.getJsonPointer());
    }

    public String getAbsoluteUri(String jsonPointer) {
        if (jsonPointer.isEmpty()) {
            return baseUri.toString();
        } else if (jsonPointer.startsWith("#")) {
            return baseUri + jsonPointer;
        } else {
            return baseUri + "#" + jsonPointer;
        }
    }

    public void registerSchema(String uri, Schema schema) {
        if (schemaCache.containsKey(uri)) {
            throw new IllegalArgumentException("Duplicate schema registration, uri=" + uri);
        }
        schemaCache.put(uri, schema);
    }

    public void registerAnchor(String uri, Schema schema) {
        schemaCache.put(uri, schema);
    }

    public boolean validateSchema(String uri, JsonNode node) {
        ValidationContext ctx = new ValidationContext(URI.create(uri), schemaCache);
        return ctx.resolveRequiredSchema(uri).validate(ctx, node).isValid();
    }
}
