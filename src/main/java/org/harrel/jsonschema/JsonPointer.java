package org.harrel.jsonschema;

import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class JsonPointer {
    private final URI baseUri;
    private final Deque<Object> path;

    public JsonPointer(URI baseUri) {
        this.baseUri = baseUri;
        this.path = new LinkedList<>();
    }

    void push(String element) {
        path.offerLast(element);
    }

    void push(Integer element) {
        path.offerLast(element);
    }

    void pop() {
        path.pollLast();
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public String getFragmentString() {
        if (path.isEmpty()) {
            return "";
        } else {
            return "#/" + path.stream().map(Object::toString).collect(Collectors.joining("/"));
        }
    }

    @Override
    public String toString() {
        return baseUri + getFragmentString();
    }
}
