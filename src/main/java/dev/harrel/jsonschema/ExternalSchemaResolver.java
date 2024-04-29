package dev.harrel.jsonschema;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;

class ExternalSchemaResolver {
    private final JsonNodeFactory jsonNodeFactory;
    private final SchemaResolver schemaResolver;
    private JsonParser jsonParser;
    
    private final ConcurrentHashMap<URI, CompletableFuture<Schema>> processingMap = new ConcurrentHashMap<>();

    ExternalSchemaResolver(JsonNodeFactory jsonNodeFactory, SchemaResolver schemaResolver) {
        this.jsonNodeFactory = jsonNodeFactory;
        this.schemaResolver = schemaResolver;
    }

    void setJsonParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    Optional<Schema> resolve(URI uri, Function<JsonNode, Schema> postGetter) {
//        CompletableFuture<Schema> future = new CompletableFuture<>();
//        CompletableFuture<Schema> currentFuture = processingMap.putIfAbsent(uri, future);
//        if (currentFuture != null) {
//            return Optional.ofNullable(currentFuture.join());
//        }
//        try {
//            Optional<Schema> schema = schemaResolver.resolve(uri.toString())
//                    .toJsonNode(jsonNodeFactory)
//                    .map(node -> {
//                        jsonParser.parseRootSchema(uri, node);
//                        return postGetter.apply(node);
//                    });
//            future.complete(schema.orElse(null));
//            return schema;
//        } catch (Exception e) {
//            future.completeExceptionally(e);
//            throw e;
//        } finally {
//            processingMap.remove(uri);
//        }

        return schemaResolver.resolve(uri.toString())
                .toJsonNode(jsonNodeFactory)
                .map(node -> {
                    jsonParser.parseRootSchema(uri, node);
                    return postGetter.apply(node);
                });
    }
}
