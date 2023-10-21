package dev.harrel.jsonschema;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class SchemaRegistry {
    private State state = State.empty();

    State createSnapshot() {
        return state.copy();
    }

    void restoreSnapshot(State state) {
        this.state = state;
    }

    Schema get(String ref) {
        URI baseUri = UriUtil.getUriWithoutFragment(ref);
        String jsonPointer = UriUtil.getJsonPointer(ref);
        return get(baseUri, jsonPointer);
    }

    Schema get(URI baseUri) {
        return get(baseUri, "");
    }

    private Schema get(URI baseUri, String fragment) {
        Fragments fragments = state.getFragments(baseUri);
        return fragments.schemas.getOrDefault(fragment, fragments.additionalSchemas.get(fragment));
    }

    Schema getDynamic(String ref) {
        URI baseUri = UriUtil.getUriWithoutFragment(ref);
        String jsonPointer = UriUtil.getJsonPointer(ref);
        Fragments fragments = state.getFragments(baseUri);
        return fragments.dynamicSchemas.get(jsonPointer);
    }

    void registerAlias(URI originalUri, URI aliasUri) {
        Fragments originalFragments = state.getFragments(originalUri);
        state.fragments.put(aliasUri, originalFragments);
    }

    void registerSchema(SchemaParsingContext ctx, JsonNode schemaNode, List<EvaluatorWrapper> evaluators, Set<String> activeVocabularies) {
        Schema schema = new Schema(ctx.getParentUri(), ctx.getAbsoluteUri(schemaNode), evaluators, activeVocabularies, ctx.getVocabulariesObject());
        state.getFragments(ctx.getBaseUri()).schemas.put(schemaNode.getJsonPointer(), schema);
        registerAnchorsIfPresent(ctx, schemaNode, schema);
    }

    void registerIdentifiableSchema(SchemaParsingContext ctx,
                                    JsonNode schemaNode,
                                    List<EvaluatorWrapper> evaluators,
                                    Set<String> activeVocabularies) {
        Fragments baseFragments = state.getFragments(ctx.getBaseUri());
        Schema identifiableSchema = new Schema(ctx.getParentUri(), ctx.getAbsoluteUri(schemaNode), evaluators, activeVocabularies, ctx.getVocabulariesObject());
        baseFragments.schemas.put(schemaNode.getJsonPointer(), identifiableSchema);
        registerAnchorsIfPresent(ctx, schemaNode, identifiableSchema);
    }

    void registerEmbeddedIdentifiableSchema(SchemaParsingContext ctx,
                                            URI id,
                                            JsonNode schemaNode,
                                            List<EvaluatorWrapper> evaluators,
                                            Set<String> activeVocabularies) {
        Fragments baseFragments = state.getFragments(ctx.getBaseUri());
        Fragments idFragments = state.getFragments(UriUtil.getUriWithoutFragment(id));

        baseFragments.schemas.entrySet().stream()
                .filter(e -> e.getKey().startsWith(schemaNode.getJsonPointer()))
                .forEach(e -> {
                    String newJsonPointer = e.getKey().substring(schemaNode.getJsonPointer().length());
                    idFragments.additionalSchemas.put(newJsonPointer, e.getValue());
                });
        Schema identifiableSchema = new Schema(ctx.getParentUri(), ctx.getAbsoluteUri(schemaNode), evaluators, activeVocabularies, ctx.getVocabulariesObject());
        idFragments.schemas.put("", identifiableSchema);
        baseFragments.schemas.put(schemaNode.getJsonPointer(), identifiableSchema);
        registerAnchorsIfPresent(ctx, schemaNode, identifiableSchema);
    }

    private void registerAnchorsIfPresent(SchemaParsingContext ctx, JsonNode schemaNode, Schema schema) {
        if (!schemaNode.isObject()) {
            return;
        }
        Map<String, JsonNode> objectMap = schemaNode.asObject();
        Fragments fragments = state.getFragments(ctx.getParentUri());

        JsonNodeUtil.getStringField(objectMap, Keyword.ANCHOR)
                .ifPresent(anchorString -> fragments.additionalSchemas.put(anchorString, schema));
        JsonNodeUtil.getStringField(objectMap, Keyword.DYNAMIC_ANCHOR)
                .ifPresent(anchorString -> fragments.dynamicSchemas.put(anchorString, schema));
    }

    static final class State {
        private final Map<URI, Fragments> fragments;

        private State(Map<URI, Fragments> fragments) {
            this.fragments = fragments;
        }

        private Fragments getFragments(URI uri) {
            return fragments.computeIfAbsent(uri, key -> Fragments.empty());
        }

        private State copy() {
            Map<URI, Fragments> copiedMap = this.fragments.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().copy()));
            return new State(copiedMap);
        }

        private static State empty() {
            return new State(new HashMap<>());
        }
    }

    private static final class Fragments {
        private final Map<String, Schema> schemas;
        private final Map<String, Schema> additionalSchemas;
        private final Map<String, Schema> dynamicSchemas;

        private Fragments(Map<String, Schema> schemas, Map<String, Schema> additionalSchemas, Map<String, Schema> dynamicSchemas) {
            this.schemas = schemas;
            this.additionalSchemas = additionalSchemas;
            this.dynamicSchemas = dynamicSchemas;
        }

        private Fragments copy() {
            return new Fragments(new HashMap<>(this.schemas), new HashMap<>(this.additionalSchemas), new HashMap<>(this.dynamicSchemas));
        }

        private static Fragments empty() {
            return new Fragments(new HashMap<>(), new HashMap<>(), new HashMap<>());
        }
    }
}
