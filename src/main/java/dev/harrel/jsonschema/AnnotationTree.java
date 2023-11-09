package dev.harrel.jsonschema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AnnotationTree {
    private final Map<String, Node> lookupMap = new HashMap<>();
    private final Node rootNode;

    public AnnotationTree() {
        this.rootNode = new Node();
        lookupMap.put("", rootNode);
    }

    Stream<Annotation> getAllAnnotations() {
        return rootNode.stream();
    }

    Node getNode(String location) {
        return lookupMap.get(location);
    }

    Node get(String parentLocation, String location) {
        boolean contained = lookupMap.containsKey(location);
        Node node = lookupMap.computeIfAbsent(location, key -> new Node());
        if (parentLocation == null) {
            return node;
        }
        if (!contained) {
            Node parentNode = lookupMap.get(parentLocation);
            parentNode.nodes.add(node);
        }
        return node;
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

