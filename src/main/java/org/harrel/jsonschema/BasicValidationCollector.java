package org.harrel.jsonschema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicValidationCollector implements ValidationCollector<List<String>> {

    private final List<String> collected = new ArrayList<>();

    @Override
    public void onSuccess(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode) {
        collected.add(schemaNode.getJsonPointer() + ", " + instanceNode.getJsonPointer() + " - VALID");
    }

    @Override
    public void onFailure(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode, String errorMessage) {
        collected.add(schemaNode.getJsonPointer() + ", " + instanceNode.getJsonPointer() + " - " + errorMessage);
    }

    @Override
    public List<String> getOutput() {
        return Collections.unmodifiableList(collected);
    }
}
