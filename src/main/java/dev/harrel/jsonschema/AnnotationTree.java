package dev.harrel.jsonschema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AnnotationTree {
    private final Map<String, Node> lookupMap = new HashMap<>();

    AnnotationTree() {
        lookupMap.put(null, new Node());
    }

    Stream<Annotation> getAllAnnotations() {
        return lookupMap.get(null).stream();
    }

    Node getNode(String location) {
        return lookupMap.get(location);
    }

    Node createIfAbsent(String parentLocation, String location) {
        return lookupMap.computeIfAbsent(location, key -> {
            Node node = new Node();
            lookupMap.get(parentLocation).nodes.add(node);
            return node;
        });
    }

    static class Node {
        final List<Node> nodes = new ArrayList<>();
        final List<Annotation> annotations = new ArrayList<>();

        Stream<Annotation> stream() {
            return Stream.concat(
                    nodes.stream().flatMap(Node::stream),
                    annotations.stream()
            );
        }
    }
}

