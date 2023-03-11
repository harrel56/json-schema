package org.harrel.jsonschema;

import java.net.URI;

abstract class AbstractContext {
    final URI baseUri;

    AbstractContext(URI baseUri) {
        this.baseUri = baseUri;
    }

    public String getAbsoluteUri(JsonNode node) {
        return getAbsoluteUri(node.getJsonPointer());
    }

    public String getAbsoluteUri(String jsonPointer) {
        if (jsonPointer.isEmpty()) {
            return baseUri + "#";
        } else if (jsonPointer.startsWith("#")) {
            return baseUri + jsonPointer;
        } else {
            return baseUri + "#" + jsonPointer;
        }
    }
}
