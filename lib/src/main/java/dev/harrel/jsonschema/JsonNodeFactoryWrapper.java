package dev.harrel.jsonschema;

import dev.harrel.jsonschema.internal.StandaloneNode;

final class JsonNodeFactoryWrapper implements JsonNodeFactory {
    private final JsonNodeFactory delegate;

    JsonNodeFactoryWrapper(JsonNodeFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public JsonNode wrap(Object node) {
        if (node instanceof StandaloneNode) {
            StandaloneNode sNode = (StandaloneNode) node;
            return sNode.getJsonPointer().isEmpty() ? sNode : sNode.copy("");
        }
        return delegate.wrap(node);
    }

    @Override
    public JsonNode create(String rawJson) {
        return delegate.create(rawJson);
    }
}
