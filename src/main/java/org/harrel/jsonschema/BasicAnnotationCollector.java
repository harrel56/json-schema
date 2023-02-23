package org.harrel.jsonschema;

import java.util.*;

public class BasicAnnotationCollector implements AnnotationCollector<List<String>> {

    private final List<String> collected = new ArrayList<>();
    private final Set<String> evaluatedPaths = new HashSet<>();

    @Override
    public void onSuccess(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode) {
        evaluatedPaths.add(instanceNode.getJsonPointer());
        collected.add(schemaNode.getJsonPointer() + ", " + instanceNode.getJsonPointer() + " - VALID");
    }

    @Override
    public void onFailure(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode, String errorMessage) {
        evaluatedPaths.add(instanceNode.getJsonPointer());
        collected.add(schemaNode.getJsonPointer() + ", " + instanceNode.getJsonPointer() + " - " + errorMessage);
    }

    @Override
    public List<String> getOutput() {
        return Collections.unmodifiableList(collected);
    }

    @Override
    public Set<String> getEvaluatedPaths() {
        return Collections.unmodifiableSet(evaluatedPaths);
    }
}
