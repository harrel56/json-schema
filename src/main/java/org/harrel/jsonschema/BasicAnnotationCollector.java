package org.harrel.jsonschema;

import java.util.*;

public class BasicAnnotationCollector implements AnnotationCollector<List<String>> {

    private final List<Annotation> annotations = new ArrayList<>();

    @Override
    public void onSuccess(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode) {
        annotations.add(new Annotation(schemaNode.getJsonPointer(), instanceNode.getJsonPointer(), schemaNode.asString(), true));
    }

    @Override
    public void onFailure(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode, String errorMessage) {
        annotations.add(new Annotation(schemaNode.getJsonPointer(), instanceNode.getJsonPointer(), errorMessage, false));
    }

    @Override
    public List<String> getOutput() {
        return annotations.stream()
                .map(a -> "schema: %s, instance: %s [%s]".formatted(a.schemaPath(), a.instancePath(), a.message()))
                .toList();
    }

    @Override
    public List<Annotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }
}
