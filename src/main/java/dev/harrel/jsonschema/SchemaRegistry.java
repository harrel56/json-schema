package dev.harrel.jsonschema;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SchemaRegistry {
    private State state = State.empty();

    State createSnapshot() {
        return State.copyOf(this.state);
    }

    void restoreSnapshot(State state) {
        this.state = state;
    }

    Schema get(String uri) {
        return state.schemas.getOrDefault(uri, state.additionalSchemas.get(uri));
    }

    Schema getDynamic(String anchor) {
        return state.dynamicSchemas.get(anchor);
    }

    void registerSchema(SchemaParsingContext ctx, JsonNode schemaNode, List<EvaluatorWrapper> evaluators) {
        Schema schema = new Schema(ctx.getParentUri(), ctx.getAbsoluteUri(schemaNode), evaluators);
        state.schemas.put(ctx.getAbsoluteUri(schemaNode), schema);
        registerAnchorsIfPresent(ctx, schemaNode, schema);
    }

    void registerIdentifiableSchema(SchemaParsingContext ctx, URI id, JsonNode schemaNode, List<EvaluatorWrapper> evaluators) {
        String absoluteUri = ctx.getAbsoluteUri(schemaNode);
        state.schemas.entrySet().stream()
                .filter(e -> e.getKey().startsWith(absoluteUri))
                .forEach(e -> {
                    /* Special case for root json pointer, because it ends with slash */
                    int normalizedUriSize = absoluteUri.endsWith("/") ? absoluteUri.length() - 1 : absoluteUri.length();
                    String newJsonPointer = e.getKey().substring(normalizedUriSize);
                    String newUri = id.toString() + "#" + newJsonPointer;
                    state.additionalSchemas.put(newUri, e.getValue());
                });
        Schema identifiableSchema = new Schema(ctx.getParentUri(), absoluteUri, evaluators);
        state.schemas.put(id.toString(), identifiableSchema);
        state.schemas.put(absoluteUri, identifiableSchema);
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
            state.additionalSchemas.put(anchoredUri, schema);
        }
        JsonNode dynamicAnchorNode = objectMap.get(Keyword.DYNAMIC_ANCHOR);
        if (dynamicAnchorNode != null && dynamicAnchorNode.isString()) {
            String anchorFragment = "#" + dynamicAnchorNode.asString();
            state.dynamicSchemas.put(ctx.getParentUri().toString() + anchorFragment, schema);
        }
    }

    static class State {
        private final Map<String, Schema> schemas;
        private final Map<String, Schema> additionalSchemas;
        private final Map<String, Schema> dynamicSchemas;

        private State(Map<String, Schema> schemas, Map<String, Schema> additionalSchemas, Map<String, Schema> dynamicSchemas) {
            this.schemas = new HashMap<>(schemas);
            this.additionalSchemas = new HashMap<>(additionalSchemas);
            this.dynamicSchemas = new HashMap<>(dynamicSchemas);
        }

        private static State empty() {
            return new State(new HashMap<>(), new HashMap<>(), new HashMap<>());
        }

        private static State copyOf(State other) {
            return new State(new HashMap<>(other.schemas), new HashMap<>(other.additionalSchemas), new HashMap<>(other.dynamicSchemas));
        }
    }
}
