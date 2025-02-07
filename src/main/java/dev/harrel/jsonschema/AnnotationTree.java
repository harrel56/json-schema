package dev.harrel.jsonschema;

import java.util.*;

final class AnnotationTree {
    private final Map<String, Node> lookupMap = new HashMap<>();

    AnnotationTree() {
        lookupMap.put(null, new Node());
    }

    List<Annotation> getAllAnnotations() {
        return lookupMap.get(null).toList();
    }

    Node getNode(String location) {
        return lookupMap.get(location);
    }

    Node createIfAbsent(LinkedList<String> evaluationStack) {
        String location = evaluationStack.element();
        Node res = lookupMap.get(location);
        if (res == null) {
            res = new Node();
            lookupMap.get(evaluationStack.size() > 1 ? evaluationStack.get(1) : null).nodes.add(res);
            lookupMap.put(location, res);
        }
        return res;
    }

    static class Node {
        final List<Node> nodes = new ArrayList<>();
        final List<Annotation> annotations = new ArrayList<>();

        List<Annotation> toList() {
            List<Annotation> result = new ArrayList<>();
            for (Node node : nodes) {
                result.addAll(node.toList());
            }
            result.addAll(annotations);
            return result;
        }
    }
}

